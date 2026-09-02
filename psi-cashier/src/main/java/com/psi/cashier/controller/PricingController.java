package com.psi.cashier.controller;

import com.psi.cashier.entity.PricingConfigEntity;
import com.psi.cashier.service.PricingEngineService;
import com.psi.cashier.service.PricingEngineService.PriceChain;
import com.psi.common.context.UserContext;
import com.psi.common.result.CommonResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 定价引擎控制器
 *
 * 提供定价配置管理 + 价格计算接口
 *
 * 核心接口：
 * - GET  /config         获取当前定价配置
 * - POST /config          保存/更新定价配置
 * - POST /calc/zmw        ZMW采购价 → 完整价格链
 * - POST /calc/usd        USD采购价 → 完整价格链
 * - GET  /calc/fx-profit  计算汇率利润
 *
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/psi/cashier/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingEngineService pricingEngineService;

    // ===== 定价配置管理 =====

    /**
     * 获取当前定价配置
     */
    @GetMapping("/config")
    public CommonResult<PricingConfigEntity> getConfig() {
        String tenantId = UserContext.getTenantId();
        return CommonResult.success(pricingEngineService.getConfig(tenantId));
    }

    /**
     * 保存/更新定价配置（仅管理员）
     * 修改汇率或加价系数后，所有后续计算自动使用新值
     */
    @PostMapping("/config")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public CommonResult<Void> saveConfig(@RequestBody PricingConfigEntity config) {
        config.setTenantId(UserContext.getTenantId());
        pricingEngineService.saveConfig(config);
        return CommonResult.success();
    }

    // ===== 价格计算 =====

    /**
     * ZMW采购价 → 完整价格链
     *
     * 自动走完三层加价：
     * 1. ZMW ÷ purchaseRate = USD成本
     * 2. USD成本 × markupFactor = USD售价
     * 3. USD售价 × salesRate = ZMW零售价
     *
     * 返回完整价格链 + 毛利 + 毛利率
     */
    @PostMapping("/calc/zmw")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public CommonResult<PriceChain> calcFromZmw(@RequestParam BigDecimal zmwAmount) {
        String tenantId = UserContext.getTenantId();
        PriceChain chain = pricingEngineService.calcFullChainFromZmw(tenantId, zmwAmount);
        return CommonResult.success(chain);
    }

    /**
     * USD采购价 → 完整价格链
     *
     * 跳过第1层（已经是USD），走第2、3层：
     * 1. (跳过，已是USD)
     * 2. USD成本 × markupFactor = USD售价
     * 3. USD售价 × salesRate = ZMW零售价
     */
    @PostMapping("/calc/usd")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public CommonResult<PriceChain> calcFromUsd(@RequestParam BigDecimal usdAmount) {
        String tenantId = UserContext.getTenantId();
        PriceChain chain = pricingEngineService.calcFullChainFromUsd(tenantId, usdAmount);
        return CommonResult.success(chain);
    }

    /**
     * 快速试算（无权限限制，方便收银时临时算价）
     * 输入ZMW采购价，返回ZMW建议零售价
     */
    @GetMapping("/calc/quick")
    public CommonResult<QuickPriceVO> quickCalc(@RequestParam BigDecimal zmwAmount) {
        String tenantId = UserContext.getTenantId();
        PriceChain chain = pricingEngineService.calcFullChainFromZmw(tenantId, zmwAmount);
        QuickPriceVO vo = new QuickPriceVO();
        vo.setZmwRetailPrice(chain.getZmwRetailPrice());
        vo.setUsdSalePrice(chain.getUsdSalePrice());
        vo.setGrossMargin(chain.getGrossMargin());
        return CommonResult.success(vo);
    }

    /**
     * 计算汇率利润
     */
    @GetMapping("/calc/fx-profit")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public CommonResult<BigDecimal> calcFxProfit(@RequestParam BigDecimal usdSalePrice) {
        String tenantId = UserContext.getTenantId();
        return CommonResult.success(pricingEngineService.calcFxProfit(tenantId, usdSalePrice));
    }

    // ===== VO =====

    @Data
    public static class QuickPriceVO {
        /** ZMW建议零售价（客户看到的） */
        private BigDecimal zmwRetailPrice;
        /** USD售价（大客户看的） */
        private BigDecimal usdSalePrice;
        /** 毛利率（%） */
        private BigDecimal grossMargin;
    }
}
