package com.psi.cashier.service;

import com.psi.cashier.entity.PricingConfigEntity;

import java.math.BigDecimal;

/**
 * 定价引擎服务
 *
 * 三层加价模型：
 * 1. 采购端：ZMW成本 ÷ purchaseRate(低于市场) = USD成本
 * 2. 加价端：USD成本 × markupFactor = USD售价
 * 3. 销售端：USD售价 × salesRate(高于市场) = ZMW零售价
 *
 * 客户只看到ZMW零售价。
 * 大客户走USD直谈，不经过ZMW汇率层。
 *
 * @author PSI
 * @version 1.0.0
 */
public interface PricingEngineService {

    /**
     * 获取当前定价配置
     */
    PricingConfigEntity getConfig(String tenantId);

    /**
     * 保存或更新定价配置
     */
    void saveConfig(PricingConfigEntity config);

    // ===== 三层加价计算 =====

    /**
     * 第1层：ZMW采购价 → USD成本
     * USD成本 = ZMW采购价 ÷ purchaseRate(低于市场汇率)
     *
     * @param zmwAmount ZMW采购金额
     * @return USD成本价
     */
    BigDecimal calcUsdCostFromZmw(String tenantId, BigDecimal zmwAmount);

    /**
     * 第2层：USD成本 → USD售价
     * USD售价 = USD成本 × markupFactor(加价系数)
     *
     * @param usdCost USD成本价
     * @return USD售价
     */
    BigDecimal calcUsdSalePrice(String tenantId, BigDecimal usdCost);

    /**
     * 第3层：USD售价 → ZMW零售价（客户看到的）
     * ZMW零售价 = USD售价 × salesRate(高于市场汇率)
     *
     * @param usdSalePrice USD售价
     * @return ZMW零售价
     */
    BigDecimal calcZmwRetailPrice(String tenantId, BigDecimal usdSalePrice);

    /**
     * 一键全链路计算：ZMW采购价 → ZMW零售价
     * 自动走完三层加价
     *
     * @param zmwPurchaseAmount ZMW采购金额
     * @return 完整价格链
     */
    PriceChain calcFullChainFromZmw(String tenantId, BigDecimal zmwPurchaseAmount);

    /**
     * 一键全链路计算：USD采购价 → ZMW零售价
     * 跳过第1层（已经是USD了），走第2、3层
     *
     * @param usdPurchaseAmount USD采购金额
     * @return 完整价格链
     */
    PriceChain calcFullChainFromUsd(String tenantId, BigDecimal usdPurchaseAmount);

    /**
     * 计算单笔交易的汇率利润
     * 汇率利润 = (salesRate - marketRate) × USD售价
     *
     * @param usdSalePrice USD售价
     * @return 汇率利润（ZMW）
     */
    BigDecimal calcFxProfit(String tenantId, BigDecimal usdSalePrice);

    /**
     * 价格链结果
     */
    @lombok.Data
    class PriceChain {
        /** USD成本价 */
        private BigDecimal usdCost;
        /** USD售价 */
        private BigDecimal usdSalePrice;
        /** ZMW零售价（客户看到的） */
        private BigDecimal zmwRetailPrice;
        /** 汇率利润（ZMW） */
        private BigDecimal fxProfit;
        /** 总毛利（ZMW，按市场汇率算真实成本） */
        private BigDecimal grossProfit;
        /** 毛利率（%） */
        private BigDecimal grossMargin;
    }
}
