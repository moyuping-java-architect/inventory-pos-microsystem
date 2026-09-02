package com.psi.purchase.service.impl;

import com.psi.purchase.entity.PurchaseOrderItemEntity;
import com.psi.purchase.mapper.PurchaseOrderItemMapper;
import com.psi.purchase.mq.producer.PurchaseSyncProducer;
import com.psi.purchase.service.PurchaseOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 采购订单明细服务实现
 */
@Slf4j
@Service
public class PurchaseOrderItemServiceImpl extends ServiceImpl<PurchaseOrderItemMapper, PurchaseOrderItemEntity> implements PurchaseOrderItemService {

    private final PurchaseSyncProducer purchaseSyncProducer;

    public PurchaseOrderItemServiceImpl(PurchaseSyncProducer purchaseSyncProducer) {
        this.purchaseSyncProducer = purchaseSyncProducer;
    }

    @Override
    public boolean save(PurchaseOrderItemEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendPurchaseOrderItem(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<PurchaseOrderItemEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result && entityList != null) {
            entityList.forEach(this::sendPurchaseOrderItem);
        }
        return result;
    }

    @Override
    public boolean updateById(PurchaseOrderItemEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendPurchaseOrderItem(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(PurchaseOrderItemEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendPurchaseOrderItem(entity);
        }
        return result;
    }

    private void sendPurchaseOrderItem(PurchaseOrderItemEntity entity) {
        try {
            purchaseSyncProducer.sendPurchaseOrderItem(entity);
        } catch (Exception e) {
            log.error("采购订单明细实时同步消息发送失败", e);
        }
    }
}
