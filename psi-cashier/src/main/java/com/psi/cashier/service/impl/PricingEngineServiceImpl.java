package com.psi.cashier.service.impl;

import com.psi.cashier.entity.PricingConfigEntity;
import com.psi.cashier.mapper.PricingConfigMapper;
import com.psi.cashier.service.PricingEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 定价引擎服务实现
 *
 * 三层加价模型：
 *   采购端：ZMW ÷ purchaseRate(低) = USD成本
 *   加价端：USD成本 × markupFactor = USD售价
 *   销售端：USD售价 × salesRate(高) = ZMW零售价
 *
 * 客户只看到ZMW零售价，三层加价全隐藏。
 *
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PricingEngineServiceImpl implements PricingEngineService {

    private final PricingConfigMapper pricingConfigMapper;

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 默认值（首次使用时自动创建） */
    private static final BigDecimal DEFAULT_MARKUP = new BigDecimal("1.26");
    private static final BigDecimal DEFAULT_PURCHASE_RATE = new BigDecimal("26.0");
    private static final BigDecimal DEFAULT_SALES_RATE = new BigDecimal("28.0");
    private static final BigDecimal DEFAULT_MARKET_RATE = new BigDecimal("27.0");
    private static final int SCALE = 4;

    @Override
    public PricingConfigEntity getConfig(String tenantId) {
        PricingConfigEntity config = pricingConfigMapper.selectByTenant(tenantId);
        if (config == null) {
            // 首次使用，自动创建默认配置
            config = createDefaultConfig(tenantId);
            pricingConfigMapper.insert(config);
            log.info("定价配置初始化: tenantId={}, markup={}, purchaseRate={}, salesRate={}",
                    tenantId, config.getMarkupFactor(), config.getPurchaseRate(), config.getSalesRate());
        }
        return config;
    }

    @Override
    public void saveConfig(PricingConfigEntity config) {
        String now = LocalDateTime.now().format(DT_FORMATTER);
        config.setUpdateTime(now);
        if (config.getId() != null) {
            pricingConfigMapper.updateById(config);
        } else {
            config.setCreateTime(now);
            pricingConfigMapper.insert(config);
        }
        log.info("定价配置更新: tenantId={}, markup={}, purchaseRate={}, salesRate={}, marketRate={}",
                config.getTenantId(), config.getMarkupFactor(),
                config.getPurchaseRate(), config.getSalesRate(), config.getMarketRate());
    }

    // ===== 第1层：ZMW采购价 → USD成本 =====

    @Override
    public BigDecimal calcUsdCostFromZmw(String tenantId, BigDecimal zmwAmount) {
        if (zmwAmount == null || zmwAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        PricingConfigEntity config = getConfig(tenantId);
        // USD成本 = ZMW采购价 ÷ purchaseRate（低于市场汇率，除以小数 → USD算高）
        BigDecimal usdCost = zmwAmount.divide(config.getPurchaseRate(), SCALE, RoundingMode.HALF_UP);
        log.debug("第1层 ZMW→USD成本: {} ÷ {} = {}", zmwAmount, config.getPurchaseRate(), usdCost);
        return usdCost;
    }

    // ===== 第2层：USD成本 → USD售价 =====

    @Override
    public BigDecimal calcUsdSalePrice(String tenantId, BigDecimal usdCost) {
        if (usdCost == null || usdCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        PricingConfigEntity config = getConfig(tenantId);
        // USD售价 = USD成本 × markupFactor
        BigDecimal usdSalePrice = usdCost.multiply(config.getMarkupFactor())
                .setScale(SCALE, RoundingMode.HALF_UP);
        log.debug("第2层 USD成本→USD售价: {} × {} = {}", usdCost, config.getMarkupFactor(), usdSalePrice);
        return usdSalePrice;
    }

    // ===== 第3层：USD售价 → ZMW零售价 =====

    @Override
    public BigDecimal calcZmwRetailPrice(String tenantId, BigDecimal usdSalePrice) {
        if (usdSalePrice == null || usdSalePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        PricingConfigEntity config = getConfig(tenantId);
        // ZMW零售价 = USD售价 × salesRate（高于市场汇率，乘大数 → ZMW算高）
        BigDecimal zmwRetail = usdSalePrice.multiply(config.getSalesRate())
                .setScale(2, RoundingMode.HALF_UP);
        log.debug("第3层 USD售价→ZMW零售: {} × {} = {}", usdSalePrice, config.getSalesRate(), zmwRetail);
        return zmwRetail;
    }

    // ===== 一键全链路计算 =====

    @Override
    public PriceChain calcFullChainFromZmw(String tenantId, BigDecimal zmwPurchaseAmount) {
        PricingConfigEntity config = getConfig(tenantId);

        // 第1层：ZMW ÷ purchaseRate = USD成本
        BigDecimal usdCost = calcUsdCostFromZmw(tenantId, zmwPurchaseAmount);

        // 第2层：USD成本 × markupFactor = USD售价
        BigDecimal usdSalePrice = calcUsdSalePrice(tenantId, usdCost);

        // 第3层：USD售价 × salesRate = ZMW零售价
        BigDecimal zmwRetailPrice = calcZmwRetailPrice(tenantId, usdSalePrice);

        // 汇率利润 = (salesRate - marketRate) × USD售价
        BigDecimal fxProfit = calcFxProfit(tenantId, usdSalePrice);

        // 真实成本（按市场汇率算）= ZMW采购价（已经是ZMW了）
        // 真实毛利 = ZMW零售价 - ZMW采购价
        BigDecimal grossProfit = zmwRetailPrice.subtract(zmwPurchaseAmount);

        // 毛利率 = 毛利 / ZMW零售价 × 100
        BigDecimal grossMargin = zmwRetailPrice.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(zmwRetailPrice, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        PriceChain chain = new PriceChain();
        chain.setUsdCost(usdCost);
        chain.setUsdSalePrice(usdSalePrice);
        chain.setZmwRetailPrice(zmwRetailPrice);
        chain.setFxProfit(fxProfit);
        chain.setGrossProfit(grossProfit);
        chain.setGrossMargin(grossMargin);

        log.info("完整价格链 ZMW→ZMW: 采购{} → USD成本{} → USD售价{} → ZMW零售{} | 汇率利润{} | 毛利{}({}%)",
                zmwPurchaseAmount, usdCost, usdSalePrice, zmwRetailPrice, fxProfit, grossProfit, grossMargin);

        return chain;
    }

    @Override
    public PriceChain calcFullChainFromUsd(String tenantId, BigDecimal usdPurchaseAmount) {
        // USD采购价已经是USD了，跳过第1层
        BigDecimal usdCost = usdPurchaseAmount;

        // 第2层：USD成本 × markupFactor = USD售价
        BigDecimal usdSalePrice = calcUsdSalePrice(tenantId, usdCost);

        // 第3层：USD售价 × salesRate = ZMW零售价
        BigDecimal zmwRetailPrice = calcZmwRetailPrice(tenantId, usdSalePrice);

        // 汇率利润
        BigDecimal fxProfit = calcFxProfit(tenantId, usdSalePrice);

        // 真实成本（按市场汇率算）= USD采购价 × marketRate
        BigDecimal realCostZmw = usdPurchaseAmount.multiply(config_getMarketRate(tenantId))
                .setScale(2, RoundingMode.HALF_UP);

        // 真实毛利 = ZMW零售价 - 真实成本(ZMW)
        BigDecimal grossProfit = zmwRetailPrice.subtract(realCostZmw);

        BigDecimal grossMargin = zmwRetailPrice.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(zmwRetailPrice, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        PriceChain chain = new PriceChain();
        chain.setUsdCost(usdCost);
        chain.setUsdSalePrice(usdSalePrice);
        chain.setZmwRetailPrice(zmwRetailPrice);
        chain.setFxProfit(fxProfit);
        chain.setGrossProfit(grossProfit);
        chain.setGrossMargin(grossMargin);

        log.info("完整价格链 USD→ZMW: 采购${} → USD售价${} → ZMW零售{} | 汇率利润{} | 毛利{}({}%)",
                usdPurchaseAmount, usdSalePrice, zmwRetailPrice, fxProfit, grossProfit, grossMargin);

        return chain;
    }

    @Override
    public BigDecimal calcFxProfit(String tenantId, BigDecimal usdSalePrice) {
        if (usdSalePrice == null || usdSalePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        PricingConfigEntity config = getConfig(tenantId);
        // 汇率利润 = (salesRate - marketRate) × USD售价
        BigDecimal rateDiff = config.getSalesRate().subtract(config.getMarketRate());
        BigDecimal fxProfit = rateDiff.multiply(usdSalePrice)
                .setScale(2, RoundingMode.HALF_UP);
        return fxProfit;
    }

    // ===== 私有方法 =====

    private BigDecimal config_getMarketRate(String tenantId) {
        return getConfig(tenantId).getMarketRate();
    }

    private PricingConfigEntity createDefaultConfig(String tenantId) {
        PricingConfigEntity config = new PricingConfigEntity();
        config.setTenantId(tenantId);
        config.setBaseCurrency("USD");
        config.setLocalCurrency("ZMW");
        config.setMarkupFactor(DEFAULT_MARKUP);
        config.setPurchaseRate(DEFAULT_PURCHASE_RATE);
        config.setSalesRate(DEFAULT_SALES_RATE);
        config.setMarketRate(DEFAULT_MARKET_RATE);
        config.setAutoUpdateRetail(1);
        String now = LocalDateTime.now().format(DT_FORMATTER);
        config.setCreateTime(now);
        config.setUpdateTime(now);
        return config;
    }
}
