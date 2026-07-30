package com.psi.purchase.service.impl;

import com.psi.purchase.entity.PurchaseOrderExtEntity;
import com.psi.purchase.mapper.PurchaseOrderExtMapper;
import com.psi.purchase.mq.producer.PurchaseSyncProducer;
import com.psi.purchase.service.PurchaseOrderExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class PurchaseOrderExtServiceImpl extends ServiceImpl<PurchaseOrderExtMapper, PurchaseOrderExtEntity> implements PurchaseOrderExtService {

    private final PurchaseSyncProducer purchaseSyncProducer;

    public PurchaseOrderExtServiceImpl(PurchaseSyncProducer purchaseSyncProducer) {
        this.purchaseSyncProducer = purchaseSyncProducer;
    }

    @Override
    public boolean save(PurchaseOrderExtEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendPurchaseOrderExt(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<PurchaseOrderExtEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result && entityList != null) {
            entityList.forEach(this::sendPurchaseOrderExt);
        }
        return result;
    }

    @Override
    public boolean updateById(PurchaseOrderExtEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendPurchaseOrderExt(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(PurchaseOrderExtEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendPurchaseOrderExt(entity);
        }
        return result;
    }

    private void sendPurchaseOrderExt(PurchaseOrderExtEntity entity) {
        try {
            purchaseSyncProducer.sendPurchaseOrderExt(entity);
        } catch (Exception e) {
            log.error("采购订单扩展实时同步消息发送失败", e);
        }
    }
}
