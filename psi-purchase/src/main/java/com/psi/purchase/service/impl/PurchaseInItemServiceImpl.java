package com.psi.purchase.service.impl;

import com.psi.purchase.entity.PurchaseInItemEntity;
import com.psi.purchase.mapper.PurchaseInItemMapper;
import com.psi.purchase.mq.producer.PurchaseSyncProducer;
import com.psi.purchase.service.PurchaseInItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 采购入库明细服务实现
 */
@Slf4j
@Service
public class PurchaseInItemServiceImpl extends ServiceImpl<PurchaseInItemMapper, PurchaseInItemEntity> implements PurchaseInItemService {

    private final PurchaseSyncProducer purchaseSyncProducer;

    public PurchaseInItemServiceImpl(PurchaseSyncProducer purchaseSyncProducer) {
        this.purchaseSyncProducer = purchaseSyncProducer;
    }

    @Override
    public boolean save(PurchaseInItemEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendPurchaseInItem(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<PurchaseInItemEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result && entityList != null) {
            entityList.forEach(this::sendPurchaseInItem);
        }
        return result;
    }

    @Override
    public boolean updateById(PurchaseInItemEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendPurchaseInItem(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(PurchaseInItemEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendPurchaseInItem(entity);
        }
        return result;
    }

    private void sendPurchaseInItem(PurchaseInItemEntity entity) {
        try {
            purchaseSyncProducer.sendPurchaseInItem(entity);
        } catch (Exception e) {
            log.error("采购入库明细实时同步消息发送失败", e);
        }
    }
}
