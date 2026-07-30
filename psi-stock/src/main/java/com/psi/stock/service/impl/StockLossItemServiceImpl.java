package com.psi.stock.service.impl;

import com.psi.stock.entity.StockLossItemEntity;
import com.psi.stock.mapper.StockLossItemMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockLossItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
public class StockLossItemServiceImpl extends ServiceImpl<StockLossItemMapper, StockLossItemEntity> implements StockLossItemService {

    private final StockSyncProducer stockSyncProducer;

    public StockLossItemServiceImpl(StockSyncProducer stockSyncProducer) {
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockLossItemEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockLossItemEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockLossItemEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockLossItemEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockLossItemEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockLossItemEntity entity) {
        try {
            stockSyncProducer.sendStockLossItem(entity);
        } catch (Exception e) {
            log.error("报损明细实时同步发送失败", e);
        }
    }
}
