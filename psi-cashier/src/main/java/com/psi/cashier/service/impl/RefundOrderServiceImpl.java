package com.psi.cashier.service.impl;

import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.entity.RefundOrderEntity;
import com.psi.cashier.entity.RefundOrderItemEntity;
import com.psi.cashier.mapper.RefundOrderMapper;
import com.psi.cashier.service.OrderMainService;
import com.psi.cashier.service.RefundOrderItemService;
import com.psi.cashier.service.RefundOrderService;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退货订单服务实现类
 * 实现退货订单的CRUD操作
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundOrderServiceImpl implements RefundOrderService {

    private final RefundOrderMapper refundOrderMapper;
    private final OrderMainService orderMainService;
    private final RefundOrderItemService refundOrderItemService;
    private final com.psi.cashier.service.OrderItemService orderItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundOrderEntity save(RefundOrderEntity entity) {
        refundOrderMapper.insert(entity);
        log.info("保存退货订单成功，退货单号：{}", entity.getRefundNo());
        return entity;
    }

    @Override
    public RefundOrderEntity getByRefundNo(String refundNo) {
        return refundOrderMapper.selectByRefundNo(refundNo);
    }

    @Override
    public List<RefundOrderEntity> getBySourceOrderNo(String sourceOrderNo) {
        return refundOrderMapper.selectBySourceOrderNo(sourceOrderNo);
    }

    @Override
    public List<RefundOrderEntity> getByOperatorId(Integer operatorId) {
        return refundOrderMapper.selectByOperatorId(operatorId);
    }

    @Override
    public PageResult<RefundOrderEntity> queryPage(int pageNum, int pageSize, String refundNo, Integer operatorId) {
        LambdaQueryWrapper<RefundOrderEntity> query = new LambdaQueryWrapper<>();
        if (refundNo != null && !refundNo.isEmpty()) {
            query.like(RefundOrderEntity::getRefundNo, refundNo);
        }
        if (operatorId != null) {
            query.eq(RefundOrderEntity::getOperatorId, operatorId);
        }
        query.orderByDesc(RefundOrderEntity::getCreateTime);
        IPage<RefundOrderEntity> page = refundOrderMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(RefundOrderEntity entity) {
        int rows = refundOrderMapper.updateById(entity);
        log.info("更新退货订单成功，退货单号：{}，影响行数：{}", entity.getRefundNo(), rows);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByRefundNo(String refundNo) {
        RefundOrderEntity entity = getByRefundNo(refundNo);
        if (entity == null) {
            return false;
        }
        refundOrderMapper.deleteById(entity.getId());
        log.info("删除退货订单成功，退货单号：{}", refundNo);
        return true;
    }

    @Override
    public boolean isOrderFullyRefunded(String sourceOrderNo) {
        // 获取原订单
        OrderMainEntity sourceOrder = orderMainService.getByOrderNo(sourceOrderNo);
        if (sourceOrder == null) {
            return false;
        }
        
        BigDecimal originalTotal = sourceOrder.getTotalAmount();
        if (originalTotal == null || originalTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // 获取所有退货订单
        List<RefundOrderEntity> refundOrders = getBySourceOrderNo(sourceOrderNo);
        if (refundOrders == null || refundOrders.isEmpty()) {
            return false;
        }
        
        // 计算已退货总金额
        BigDecimal refundedTotal = BigDecimal.ZERO;
        for (RefundOrderEntity refundOrder : refundOrders) {
            if (refundOrder.getTotalRefund() != null) {
                refundedTotal = refundedTotal.add(refundOrder.getTotalRefund());
            }
        }
        
        // 如果已退货金额 >= 原订单金额，认为整单已退货
        return refundedTotal.compareTo(originalTotal) >= 0;
    }

    @Override
    public String checkItemRefundQuantity(String sourceOrderNo, List<com.psi.cashier.dto.RefundItemSaveDTO> refundItems) {
        // 获取原订单商品明细
        List<com.psi.cashier.entity.OrderItemEntity> sourceItems = orderItemService.getByOrderNo(sourceOrderNo);
        if (sourceItems == null || sourceItems.isEmpty()) {
            return "原订单商品为空";
        }
        
        // 获取该订单已退货的商品明细
        List<RefundOrderItemEntity> existingRefundItems = refundOrderItemService.getBySourceOrderNo(sourceOrderNo);
        
        // 构建原订单商品数量映射
        java.util.Map<Integer, BigDecimal> originalQuantityMap = new java.util.HashMap<>();
        for (com.psi.cashier.entity.OrderItemEntity item : sourceItems) {
            originalQuantityMap.put(item.getSkuId(), item.getSaleQuantity());
        }
        
        // 构建已退货商品数量映射
        java.util.Map<Integer, BigDecimal> refundedQuantityMap = new java.util.HashMap<>();
        if (existingRefundItems != null) {
            for (RefundOrderItemEntity item : existingRefundItems) {
                BigDecimal current = refundedQuantityMap.getOrDefault(item.getSkuId(), BigDecimal.ZERO);
                refundedQuantityMap.put(item.getSkuId(), current.add(item.getRefundQuantity()));
            }
        }
        
        // 校验每个退货商品的数量
        for (com.psi.cashier.dto.RefundItemSaveDTO refundItem : refundItems) {
            Integer skuId = parseIntegerSafely(refundItem.getSkuId());
            BigDecimal refundQuantity = refundItem.getRefundQuantity();
            
            if (refundQuantity == null || refundQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                return "退货数量必须大于0";
            }
            
            BigDecimal originalQuantity = originalQuantityMap.get(skuId);
            if (originalQuantity == null) {
                return "商品[" + refundItem.getProductName() + "]不是原订单商品";
            }
            
            BigDecimal alreadyRefunded = refundedQuantityMap.getOrDefault(skuId, BigDecimal.ZERO);
            BigDecimal remaining = originalQuantity.subtract(alreadyRefunded);
            
            if (refundQuantity.compareTo(remaining) > 0) {
                return "商品[" + refundItem.getProductName() + "]退货数量(" + refundQuantity + ")超过可退货数量(" + remaining + ")";
            }
        }
        
        return null;
    }

    /**
     * 安全解析字符串为 Integer
     * 如果字符串为 null、空或无法解析为整数，则返回 null
     *
     * @param value 待解析的字符串
     * @return 解析后的 Integer，解析失败返回 null
     */
    private Integer parseIntegerSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析字符串为整数: {}", value);
            return null;
        }
    }
}