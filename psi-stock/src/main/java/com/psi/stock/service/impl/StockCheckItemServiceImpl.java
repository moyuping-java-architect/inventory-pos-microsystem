package com.psi.stock.service.impl;

import com.psi.stock.entity.StockCheckItemEntity;
import com.psi.stock.mapper.StockCheckItemMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockCheckItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
public class StockCheckItemServiceImpl extends ServiceImpl<StockCheckItemMapper, StockCheckItemEntity> implements StockCheckItemService {

    private final StockSyncProducer stockSyncProducer;

    public StockCheckItemServiceImpl(StockSyncProducer stockSyncProducer) {
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockCheckItemEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockCheckItemEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockCheckItemEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockCheckItemEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockCheckItemEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockCheckItemEntity entity) {
        try {
            stockSyncProducer.sendStockCheckItem(entity);
        } catch (Exception e) {
            log.error("盘点明细实时同步发送失败", e);
        }
    }
}
