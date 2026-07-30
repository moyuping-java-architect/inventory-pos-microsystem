package com.psi.stock.service.impl;

import com.psi.stock.entity.StockTransferItemEntity;
import com.psi.stock.mapper.StockTransferItemMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockTransferItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
public class StockTransferItemServiceImpl extends ServiceImpl<StockTransferItemMapper, StockTransferItemEntity> implements StockTransferItemService {

    private final StockSyncProducer stockSyncProducer;

    public StockTransferItemServiceImpl(StockSyncProducer stockSyncProducer) {
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockTransferItemEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockTransferItemEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockTransferItemEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockTransferItemEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockTransferItemEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockTransferItemEntity entity) {
        try {
            stockSyncProducer.sendStockTransferItem(entity);
        } catch (Exception e) {
            log.error("调拨明细实时同步发送失败", e);
        }
    }
}
