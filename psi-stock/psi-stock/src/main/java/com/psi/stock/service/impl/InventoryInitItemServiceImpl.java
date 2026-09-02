package com.psi.stock.service.impl;

import com.psi.stock.entity.InventoryInitItemEntity;
import com.psi.stock.mapper.InventoryInitItemMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.InventoryInitItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
public class InventoryInitItemServiceImpl extends ServiceImpl<InventoryInitItemMapper, InventoryInitItemEntity> implements InventoryInitItemService {

    private final StockSyncProducer stockSyncProducer;

    public InventoryInitItemServiceImpl(StockSyncProducer stockSyncProducer) {
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(InventoryInitItemEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(InventoryInitItemEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(InventoryInitItemEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<InventoryInitItemEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (InventoryInitItemEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(InventoryInitItemEntity entity) {
        try {
            stockSyncProducer.sendInventoryInitItem(entity);
        } catch (Exception e) {
            log.error("库存初始化明细实时同步发送失败", e);
        }
    }
}
