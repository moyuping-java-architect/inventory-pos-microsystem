package com.psi.stock.service.impl;

import com.psi.stock.entity.StockOverItemEntity;
import com.psi.stock.mapper.StockOverItemMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockOverItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
public class StockOverItemServiceImpl extends ServiceImpl<StockOverItemMapper, StockOverItemEntity> implements StockOverItemService {

    private final StockSyncProducer stockSyncProducer;

    public StockOverItemServiceImpl(StockSyncProducer stockSyncProducer) {
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockOverItemEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockOverItemEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockOverItemEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockOverItemEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockOverItemEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockOverItemEntity entity) {
        try {
            stockSyncProducer.sendStockOverItem(entity);
        } catch (Exception e) {
            log.error("报溢明细实时同步发送失败", e);
        }
    }
}
