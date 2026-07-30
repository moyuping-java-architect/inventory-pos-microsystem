package com.psi.stock.service.impl;

import com.psi.stock.dto.StockBatchDTO;
import com.psi.stock.dto.StockBatchQueryDTO;
import com.psi.stock.entity.StockBatchEntity;
import com.psi.stock.mapper.StockBatchMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockBatchService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;

@Slf4j
@Service
public class StockBatchServiceImpl extends ServiceImpl<StockBatchMapper, StockBatchEntity> implements StockBatchService {

    private final StockSyncProducer stockSyncProducer;

    public StockBatchServiceImpl(StockSyncProducer stockSyncProducer) {
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockBatchEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockBatchEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockBatchEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockBatchEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockBatchEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockBatchEntity entity) {
        try {
            stockSyncProducer.sendStockBatch(entity);
        } catch (Exception e) {
            log.error("批次库存实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<StockBatchDTO> getById(Long id) {
        StockBatchEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, StockBatchDTO.class));
    }

    @Override
    public PageResult<StockBatchDTO> list(StockBatchQueryDTO queryDTO) {
        Page<StockBatchEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<StockBatchEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getWarehouseCode() != null) {
            wrapper.like(StockBatchEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (queryDTO.getGoodsCode() != null) {
            wrapper.like(StockBatchEntity::getGoodsCode, queryDTO.getGoodsCode());
        }
        if (queryDTO.getBatchNo() != null) {
            wrapper.like(StockBatchEntity::getBatchNo, queryDTO.getBatchNo());
        }
        if (queryDTO.getSupplierCode() != null) {
            wrapper.like(StockBatchEntity::getSupplierCode, queryDTO.getSupplierCode());
        }
        if (queryDTO.getExpireDateStart() != null) {
            wrapper.ge(StockBatchEntity::getExpireDate, queryDTO.getExpireDateStart());
        }
        if (queryDTO.getExpireDateEnd() != null) {
            wrapper.le(StockBatchEntity::getExpireDate, queryDTO.getExpireDateEnd());
        }
        
        IPage<StockBatchEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, StockBatchDTO.class));
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        StockBatchEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        syncSend(entity);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> increaseBatch(String warehouseCode, String warehouseName, String goodsCode, 
                                             String goodsName, String goodsSpec, String unit, String batchNo,
                                             String productionDate, String expireDate, BigDecimal quantity,
                                             BigDecimal costPrice, String supplierCode, String supplierName) {
        LambdaQueryWrapper<StockBatchEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockBatchEntity::getWarehouseCode, warehouseCode)
               .eq(StockBatchEntity::getGoodsCode, goodsCode)
               .eq(StockBatchEntity::getBatchNo, batchNo);
        
        StockBatchEntity entity = super.getOne(wrapper);
        
        if (entity == null) {
            entity = new StockBatchEntity();
            entity.setWarehouseCode(warehouseCode);
            entity.setWarehouseName(warehouseName);
            entity.setGoodsCode(goodsCode);
            entity.setGoodsName(goodsName);
            entity.setGoodsSpec(goodsSpec);
            entity.setUnit(unit);
            entity.setBatchNo(batchNo);
            entity.setProductionDate(productionDate);
            entity.setExpireDate(expireDate);
            entity.setQuantity(quantity);
            entity.setLockedQuantity(BigDecimal.ZERO);
            entity.setAvailableQuantity(quantity);
            entity.setCostPrice(costPrice);
            entity.setTotalAmount(quantity.multiply(costPrice));
            entity.setSupplierCode(supplierCode);
            entity.setSupplierName(supplierName);
            super.save(entity);
            syncSend(entity);
        } else {
            entity.setQuantity(entity.getQuantity().add(quantity));
            entity.setAvailableQuantity(entity.getAvailableQuantity().add(quantity));
            entity.setTotalAmount(entity.getTotalAmount().add(quantity.multiply(costPrice)));
            super.updateById(entity);
            syncSend(entity);
        }
        
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> decreaseBatch(String warehouseCode, String goodsCode, String batchNo, BigDecimal quantity) {
        LambdaQueryWrapper<StockBatchEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockBatchEntity::getWarehouseCode, warehouseCode)
               .eq(StockBatchEntity::getGoodsCode, goodsCode)
               .eq(StockBatchEntity::getBatchNo, batchNo);
        
        StockBatchEntity entity = super.getOne(wrapper);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        if (entity.getAvailableQuantity().compareTo(quantity) < 0) {
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "批次库存不足");
        }
        
        entity.setQuantity(entity.getQuantity().subtract(quantity));
        entity.setAvailableQuantity(entity.getAvailableQuantity().subtract(quantity));
        entity.setTotalAmount(entity.getTotalAmount().subtract(quantity.multiply(entity.getCostPrice())));
        super.updateById(entity);
        syncSend(entity);
        
        return CommonResult.success();
    }
}
