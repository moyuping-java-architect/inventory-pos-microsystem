package com.psi.sale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.psi.sale.dto.PromotionCalculateDTO;
import com.psi.sale.dto.PromotionDTO;
import com.psi.sale.dto.PromotionItemDTO;
import com.psi.sale.dto.PromotionQueryDTO;
import com.psi.sale.dto.PromotionResultDTO;
import com.psi.sale.entity.PromotionEntity;
import com.psi.sale.entity.PromotionItemEntity;
import com.psi.sale.mapper.PromotionItemMapper;
import com.psi.sale.mapper.PromotionMapper;
import com.psi.sale.service.PromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, PromotionEntity> implements PromotionService {

    private final PromotionItemMapper promotionItemMapper;

    public PromotionServiceImpl(PromotionItemMapper promotionItemMapper) {
        this.promotionItemMapper = promotionItemMapper;
    }

    @Override
    public PageResult<PromotionDTO> page(PromotionQueryDTO queryDTO) {
        LambdaQueryWrapper<PromotionEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getPromotionNo())) {
            wrapper.like(PromotionEntity::getPromotionNo, queryDTO.getPromotionNo());
        }
        if (StringUtils.hasText(queryDTO.getPromotionName())) {
            wrapper.like(PromotionEntity::getPromotionName, queryDTO.getPromotionName());
        }
        if (queryDTO.getPromotionType() != null) {
            wrapper.eq(PromotionEntity::getPromotionType, queryDTO.getPromotionType());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(PromotionEntity::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(PromotionEntity::getPriority);
        wrapper.orderByDesc(PromotionEntity::getCreateTime);

        Page<PromotionEntity> page = baseMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                wrapper
        );

        List<PromotionDTO> list = page.getRecords().stream()
                .map(e -> BeanUtils.convert(e, PromotionDTO.class))
                .collect(Collectors.toList());

        return PageResult.success(list, page.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public CommonResult<PromotionDTO> getById(Long id) {
        PromotionEntity entity = baseMapper.selectById(id);
        if (entity == null) {
            return CommonResult.fail("促销活动不存在");
        }

        PromotionDTO dto = BeanUtils.convert(entity, PromotionDTO.class);

        List<PromotionItemEntity> items = promotionItemMapper.selectList(
                new LambdaQueryWrapper<PromotionItemEntity>()
                        .eq(PromotionItemEntity::getPromotionId, id)
        );
        dto.setItems(items.stream()
                .map(e -> BeanUtils.convert(e, PromotionItemDTO.class))
                .collect(Collectors.toList()));

        return CommonResult.success(dto);
    }

    @Override
    @Transactional
    public CommonResult<Void> add(PromotionDTO dto) {
        PromotionEntity entity = BeanUtils.convert(dto, PromotionEntity.class);
        baseMapper.insert(entity);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PromotionItemDTO itemDTO : dto.getItems()) {
                PromotionItemEntity item = BeanUtils.convert(itemDTO, PromotionItemEntity.class);
                item.setPromotionId(entity.getId());
                item.setPromotionNo(entity.getPromotionNo());
                promotionItemMapper.insert(item);
            }
        }

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> update(PromotionDTO dto) {
        PromotionEntity entity = BeanUtils.convert(dto, PromotionEntity.class);
        baseMapper.updateById(entity);

        promotionItemMapper.delete(
                new LambdaQueryWrapper<PromotionItemEntity>()
                        .eq(PromotionItemEntity::getPromotionId, dto.getId())
        );

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PromotionItemDTO itemDTO : dto.getItems()) {
                PromotionItemEntity item = BeanUtils.convert(itemDTO, PromotionItemEntity.class);
                item.setPromotionId(dto.getId());
                item.setPromotionNo(dto.getPromotionNo());
                promotionItemMapper.insert(item);
            }
        }

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        baseMapper.deleteById(id);
        promotionItemMapper.delete(
                new LambdaQueryWrapper<PromotionItemEntity>()
                        .eq(PromotionItemEntity::getPromotionId, id)
        );
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        PromotionEntity entity = new PromotionEntity();
        entity.setId(id);
        entity.setStatus(status);
        baseMapper.updateById(entity);
        return CommonResult.success();
    }

    @Override
    public CommonResult<List<PromotionDTO>> getActivePromotions(String warehouseCode) {
        LocalDateTime now = LocalDateTime.now();

        List<PromotionEntity> list = baseMapper.selectList(
                new LambdaQueryWrapper<PromotionEntity>()
                        .eq(PromotionEntity::getStatus, 1)
                        .le(PromotionEntity::getStartTime, now)
                        .ge(PromotionEntity::getEndTime, now)
                        .orderByAsc(PromotionEntity::getPriority)
        );

        List<PromotionDTO> result = list.stream()
                .map(e -> BeanUtils.convert(e, PromotionDTO.class))
                .collect(Collectors.toList());

        return CommonResult.success(result);
    }

    @Override
    public CommonResult<PromotionResultDTO> calculate(PromotionCalculateDTO calculateDTO) {
        LocalDateTime now = LocalDateTime.now();

        List<PromotionEntity> promotions = baseMapper.selectList(
                new LambdaQueryWrapper<PromotionEntity>()
                        .eq(PromotionEntity::getStatus, 1)
                        .le(PromotionEntity::getStartTime, now)
                        .ge(PromotionEntity::getEndTime, now)
                        .orderByAsc(PromotionEntity::getPriority)
        );

        PromotionResultDTO result = new PromotionResultDTO();
        result.setAppliedPromotions(new ArrayList<>());
        result.setItemResults(new ArrayList<>());

        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal remainingAmount = calculateDTO.getTotalAmount() != null
                ? calculateDTO.getTotalAmount() : BigDecimal.ZERO;

        List<PromotionCalculateDTO.PromotionCalculateItemDTO> items = calculateDTO.getItems();
        if (items == null || items.isEmpty()) {
            result.setDiscountAmount(BigDecimal.ZERO);
            result.setFinalAmount(remainingAmount);
            return CommonResult.success(result);
        }

        for (PromotionCalculateDTO.PromotionCalculateItemDTO item : items) {
            PromotionResultDTO.PromotionItemResultDTO itemResult = new PromotionResultDTO.PromotionItemResultDTO();
            itemResult.setSkuCode(item.getSkuCode());
            itemResult.setOriginalPrice(item.getPrice());
            itemResult.setFinalPrice(item.getPrice());
            itemResult.setDiscountAmount(BigDecimal.ZERO);
            result.getItemResults().add(itemResult);
        }

        for (PromotionEntity promotion : promotions) {
            boolean isApplicable = isPromotionApplicable(promotion, calculateDTO);
            if (!isApplicable) {
                continue;
            }

            BigDecimal discount = calculatePromotionDiscount(promotion, calculateDTO);
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                PromotionResultDTO.PromotionAppliedDTO applied = new PromotionResultDTO.PromotionAppliedDTO();
                applied.setPromotionId(promotion.getId());
                applied.setPromotionNo(promotion.getPromotionNo());
                applied.setPromotionName(promotion.getPromotionName());
                applied.setPromotionType(promotion.getPromotionType());
                applied.setDiscountAmount(discount);
                result.getAppliedPromotions().add(applied);

                totalDiscount = totalDiscount.add(discount);
                remainingAmount = remainingAmount.subtract(discount);

                distributeItemDiscount(result, discount, promotion, calculateDTO);

                if (promotion.getSuperimposable() == null || promotion.getSuperimposable() != 1) {
                    break;
                }
            }
        }

        result.setDiscountAmount(totalDiscount);
        result.setFinalAmount(remainingAmount.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO : remainingAmount);

        return CommonResult.success(result);
    }

    private boolean isPromotionApplicable(PromotionEntity promotion, PromotionCalculateDTO calculateDTO) {
        if (promotion.getScopeType() == null || promotion.getScopeType() == 1) {
            return true;
        }

        List<PromotionItemEntity> promoItems = promotionItemMapper.selectList(
                new LambdaQueryWrapper<PromotionItemEntity>()
                        .eq(PromotionItemEntity::getPromotionId, promotion.getId())
        );

        if (promoItems.isEmpty()) {
            return true;
        }

        Set<String> applicableSkus = new HashSet<>();
        Set<String> applicableCategories = new HashSet<>();
        for (PromotionItemEntity item : promoItems) {
            if (item.getItemType() != null && item.getItemType() == 1) {
                applicableSkus.add(item.getItemCode());
            } else {
                applicableCategories.add(item.getCategoryCode());
            }
        }

        for (PromotionCalculateDTO.PromotionCalculateItemDTO item : calculateDTO.getItems()) {
            if (applicableSkus.contains(item.getSkuCode())
                    || applicableCategories.contains(item.getCategoryCode())) {
                return true;
            }
        }

        return false;
    }

    private BigDecimal calculatePromotionDiscount(PromotionEntity promotion, PromotionCalculateDTO calculateDTO) {
        BigDecimal totalAmount = calculateDTO.getTotalAmount() != null
                ? calculateDTO.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal totalQty = calculateDTO.getTotalQuantity() != null
                ? calculateDTO.getTotalQuantity() : BigDecimal.ZERO;

        Integer promotionType = promotion.getPromotionType();
        Integer discountType = promotion.getDiscountType();
        BigDecimal discountValue = promotion.getDiscountValue() != null
                ? promotion.getDiscountValue() : BigDecimal.ZERO;

        if (promotionType == null) {
            return BigDecimal.ZERO;
        }

        switch (promotionType) {
            case 1:
                if (promotion.getMinAmount() != null && totalAmount.compareTo(promotion.getMinAmount()) < 0) {
                    return BigDecimal.ZERO;
                }
                if (discountType == 1) {
                    return discountValue;
                } else if (discountType == 2) {
                    return totalAmount.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                break;
            case 2:
                if (promotion.getMinQuantity() != null && totalQty.compareTo(promotion.getMinQuantity()) < 0) {
                    return BigDecimal.ZERO;
                }
                if (discountType == 2) {
                    return totalAmount.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                break;
            case 3:
                return BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    private void distributeItemDiscount(PromotionResultDTO result, BigDecimal totalDiscount,
                                       PromotionEntity promotion, PromotionCalculateDTO calculateDTO) {
        List<PromotionResultDTO.PromotionItemResultDTO> itemResults = result.getItemResults();
        List<PromotionCalculateDTO.PromotionCalculateItemDTO> items = calculateDTO.getItems();

        if (itemResults.isEmpty()) return;

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PromotionCalculateDTO.PromotionCalculateItemDTO item : items) {
            if (item.getAmount() != null) {
                totalAmount = totalAmount.add(item.getAmount());
            }
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal avgDiscount = totalDiscount.divide(BigDecimal.valueOf(itemResults.size()), 2, RoundingMode.HALF_UP);
            for (PromotionResultDTO.PromotionItemResultDTO itemResult : itemResults) {
                itemResult.setDiscountAmount(itemResult.getDiscountAmount().add(avgDiscount));
                itemResult.setFinalPrice(itemResult.getFinalPrice().subtract(avgDiscount));
            }
            return;
        }

        for (int i = 0; i < itemResults.size(); i++) {
            PromotionResultDTO.PromotionItemResultDTO itemResult = itemResults.get(i);
            PromotionCalculateDTO.PromotionCalculateItemDTO item = items.get(i);

            BigDecimal itemAmount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            BigDecimal itemDiscount = totalDiscount.multiply(itemAmount)
                    .divide(totalAmount, 2, RoundingMode.HALF_UP);

            itemResult.setDiscountAmount(itemResult.getDiscountAmount().add(itemDiscount));
            itemResult.setFinalPrice(itemResult.getFinalPrice().subtract(itemDiscount));
        }
    }
}
