package com.psi.purchase.service.impl;

import com.psi.purchase.entity.PurchaseReturnItemEntity;
import com.psi.purchase.mapper.PurchaseReturnItemMapper;
import com.psi.purchase.mq.producer.PurchaseSyncProducer;
import com.psi.purchase.service.PurchaseReturnItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 采购退货明细服务实现
 */
@Slf4j
@Service
public class PurchaseReturnItemServiceImpl extends ServiceImpl<PurchaseReturnItemMapper, PurchaseReturnItemEntity> implements PurchaseReturnItemService {

    private final PurchaseSyncProducer purchaseSyncProducer;

    public PurchaseReturnItemServiceImpl(PurchaseSyncProducer purchaseSyncProducer) {
        this.purchaseSyncProducer = purchaseSyncProducer;
    }

    @Override
    public boolean save(PurchaseReturnItemEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendPurchaseReturnItem(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<PurchaseReturnItemEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result && entityList != null) {
            entityList.forEach(this::sendPurchaseReturnItem);
        }
        return result;
    }

    @Override
    public boolean updateById(PurchaseReturnItemEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendPurchaseReturnItem(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(PurchaseReturnItemEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendPurchaseReturnItem(entity);
        }
        return result;
    }

    private void sendPurchaseReturnItem(PurchaseReturnItemEntity entity) {
        try {
            purchaseSyncProducer.sendPurchaseReturnItem(entity);
        } catch (Exception e) {
            log.error("采购退货明细实时同步消息发送失败", e);
        }
    }
}
