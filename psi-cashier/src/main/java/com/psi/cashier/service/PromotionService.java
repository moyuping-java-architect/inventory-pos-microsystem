package com.psi.cashier.service;

import com.psi.cashier.dto.CashierItemSaveDTO;
import com.psi.cashier.dto.CashierMainSaveDTO;
import com.psi.cashier.entity.PromotionEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收银端促销服务
 * 本地计算促销优惠（离线可用）
 */
public interface PromotionService {

    /**
     * 获取当前有效的促销活动列表
     */
    List<PromotionEntity> getActivePromotions();

    /**
     * 计算促销优惠金额
     *
     * @param dto 收银订单数据
     * @return 促销优惠总额（null表示无促销适用）
     */
    BigDecimal calculateDiscount(CashierMainSaveDTO dto);

    /**
     * 计算促销优惠金额（明细方式）
     *
     * @param items        商品明细
     * @param totalAmount  总金额
     * @return 促销优惠总额
     */
    BigDecimal calculateDiscount(List<CashierItemSaveDTO> items, BigDecimal totalAmount);
}
