package com.psi.cashier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psi.cashier.dto.CashierItemSaveDTO;
import com.psi.cashier.dto.CashierMainSaveDTO;
import com.psi.cashier.entity.PromotionEntity;
import com.psi.cashier.entity.PromotionItemEntity;
import com.psi.cashier.mapper.PromotionItemMapper;
import com.psi.cashier.mapper.PromotionMapper;
import com.psi.cashier.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 收银端促销服务实现
 * 本地计算促销优惠，离线可用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionMapper promotionMapper;
    private final PromotionItemMapper promotionItemMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<PromotionEntity> getActivePromotions() {
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        return promotionMapper.selectList(
                new LambdaQueryWrapper<PromotionEntity>()
                        .eq(PromotionEntity::getStatus, 1)
                        .le(PromotionEntity::getStartTime, now)
                        .ge(PromotionEntity::getEndTime, now)
                        .orderByAsc(PromotionEntity::getPriority)
        );
    }

    @Override
    public BigDecimal calculateDiscount(CashierMainSaveDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalAmount = dto.getTotalAmount() != null
                ? dto.getTotalAmount() : BigDecimal.ZERO;
        return calculateDiscount(dto.getItems(), totalAmount);
    }

    @Override
    public BigDecimal calculateDiscount(List<CashierItemSaveDTO> items, BigDecimal totalAmount) {
        List<PromotionEntity> promotions = getActivePromotions();
        if (promotions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal remainingAmount = totalAmount;
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (CashierItemSaveDTO item : items) {
            if (item.getQuantity() != null) {
                totalQuantity = totalQuantity.add(item.getQuantity());
            }
        }

        boolean appliedNonSuperimposable = false;

        for (PromotionEntity promotion : promotions) {
            if (appliedNonSuperimposable) {
                break;
            }

            // 检查促销是否适用于当前商品
            if (!isPromotionApplicable(promotion, items)) {
                continue;
            }

            BigDecimal discount = calculateSinglePromotion(promotion, remainingAmount, totalQuantity);
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                totalDiscount = totalDiscount.add(discount);
                remainingAmount = remainingAmount.subtract(discount);

                log.info("促销命中：{}，优惠金额：{}，剩余金额：{}",
                        promotion.getPromotionName(), discount, remainingAmount);

                // 不可叠加则停止
                if (promotion.getSuperimposable() == null || promotion.getSuperimposable() != 1) {
                    appliedNonSuperimposable = true;
                }
            }
        }

        return totalDiscount;
    }

    /**
     * 检查促销是否适用于当前商品列表
     */
    private boolean isPromotionApplicable(PromotionEntity promotion, List<CashierItemSaveDTO> items) {
        // scope_type=1 全部商品，直接适用
        if (promotion.getScopeType() == null || promotion.getScopeType() == 1) {
            return true;
        }

        // 指定商品/分类，需要查 promotion_item
        List<PromotionItemEntity> promoItems = promotionItemMapper.selectList(
                new LambdaQueryWrapper<PromotionItemEntity>()
                        .eq(PromotionItemEntity::getPromotionId, promotion.getId())
        );

        if (promoItems.isEmpty()) {
            return true;
        }

        Set<String> applicableGoodsCodes = new HashSet<>();
        Set<String> applicableCategoryCodes = new HashSet<>();
        for (PromotionItemEntity item : promoItems) {
            if (item.getItemType() != null && item.getItemType() == 1) {
                applicableGoodsCodes.add(item.getItemCode());
            } else {
                applicableCategoryCodes.add(item.getCategoryCode());
            }
        }

        for (CashierItemSaveDTO item : items) {
            if (applicableGoodsCodes.contains(item.getGoodsCode())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 计算单个促销的优惠金额
     */
    private BigDecimal calculateSinglePromotion(PromotionEntity promotion,
                                                BigDecimal totalAmount, BigDecimal totalQuantity) {
        Integer promotionType = promotion.getPromotionType();
        Integer discountType = promotion.getDiscountType();
        BigDecimal discountValue = promotion.getDiscountValue() != null
                ? promotion.getDiscountValue() : BigDecimal.ZERO;

        if (promotionType == null) {
            return BigDecimal.ZERO;
        }

        switch (promotionType) {
            case 1:
                // 满减：满 min_amount 减 discount_value
                if (promotion.getMinAmount() != null
                        && totalAmount.compareTo(promotion.getMinAmount()) < 0) {
                    return BigDecimal.ZERO;
                }
                if (discountType != null && discountType == 1) {
                    return discountValue.min(totalAmount);
                } else if (discountType != null && discountType == 2) {
                    return totalAmount.multiply(discountValue)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                break;

            case 2:
                // 满件折扣：满 min_quantity 享受 discount_value% 折扣
                if (promotion.getMinQuantity() != null
                        && totalQuantity.compareTo(promotion.getMinQuantity()) < 0) {
                    return BigDecimal.ZERO;
                }
                if (discountType != null && discountType == 2) {
                    return totalAmount.multiply(discountValue)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                break;

            case 3:
                // 买赠：暂不自动计算金额，由前端处理赠品
                return BigDecimal.ZERO;

            default:
                return BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }
}
