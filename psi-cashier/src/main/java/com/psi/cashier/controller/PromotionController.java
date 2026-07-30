package com.psi.cashier.controller;

import com.psi.cashier.dto.CashierItemSaveDTO;
import com.psi.cashier.dto.CashierMainSaveDTO;
import com.psi.cashier.entity.PromotionEntity;
import com.psi.cashier.service.PromotionService;
import com.psi.common.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收银端促销控制器
 * 提供促销活动查询和优惠试算接口
 */
@RestController
@RequestMapping("/psi/cashier/promotion")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * 获取当前有效的促销活动列表
     * 收银员可在收银界面看到当前有哪些促销正在进行
     */
    @GetMapping("/active")
    public CommonResult<List<PromotionEntity>> getActivePromotions() {
        List<PromotionEntity> list = promotionService.getActivePromotions();
        return CommonResult.success(list);
    }

    /**
     * 促销优惠试算
     * 收银员选好商品后，点击"计算优惠"预览能省多少钱
     */
    @PostMapping("/calculate")
    public CommonResult<Map<String, Object>> calculate(@RequestBody CashierMainSaveDTO dto) {
        BigDecimal discount = promotionService.calculateDiscount(dto);
        BigDecimal totalAmount = dto.getTotalAmount() != null
                ? dto.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount.subtract(discount).max(BigDecimal.ZERO);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("discountAmount", discount);
        result.put("finalAmount", finalAmount);
        result.put("hasPromotion", discount.compareTo(BigDecimal.ZERO) > 0);

        return CommonResult.success(result);
    }

    /**
     * 简化版试算（只传商品明细和总金额）
     */
    @PostMapping("/calculate/simple")
    public CommonResult<Map<String, Object>> calculateSimple(
            @RequestBody List<CashierItemSaveDTO> items,
            @RequestParam BigDecimal totalAmount) {
        BigDecimal discount = promotionService.calculateDiscount(items, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discount).max(BigDecimal.ZERO);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("discountAmount", discount);
        result.put("finalAmount", finalAmount);
        result.put("hasPromotion", discount.compareTo(BigDecimal.ZERO) > 0);

        return CommonResult.success(result);
    }
}
