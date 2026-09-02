package com.psi.stock.service.impl;

import com.psi.stock.dto.StockTransferItemDTO;
import com.psi.stock.dto.StockTransferItemSaveDTO;
import com.psi.stock.dto.StockTransferMainDTO;
import com.psi.stock.dto.StockTransferQueryDTO;
import com.psi.stock.dto.StockTransferSaveDTO;
import com.psi.stock.entity.StockTransferItemEntity;
import com.psi.stock.entity.StockTransferMainEntity;
import com.psi.stock.mapper.StockTransferMainMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockService;
import com.psi.stock.service.StockTransferItemService;
import com.psi.stock.service.StockTransferMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.util.BeanUtils;
import com.psi.common.util.IdUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class StockTransferMainServiceImpl extends ServiceImpl<StockTransferMainMapper, StockTransferMainEntity> implements StockTransferMainService {

    private final StockTransferItemService stockTransferItemService;
    private final BatchUtils batchUtils;
    private final StockService stockService;
    private final StockSyncProducer stockSyncProducer;

    public StockTransferMainServiceImpl(StockTransferItemService stockTransferItemService, BatchUtils batchUtils,
                                        StockService stockService, StockSyncProducer stockSyncProducer) {
        this.stockTransferItemService = stockTransferItemService;
        this.batchUtils = batchUtils;
        this.stockService = stockService;
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockTransferMainEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockTransferMainEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockTransferMainEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockTransferMainEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockTransferMainEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockTransferMainEntity entity) {
        try {
            stockSyncProducer.sendStockTransferMain(entity);
        } catch (Exception e) {
            log.error("调拨主单实时同步发送失败", e);
        }
    }

    private BigDecimal getConversionRate(String skuCode, String unitCode, BigDecimal itemRate) {
        if (itemRate != null && itemRate.compareTo(BigDecimal.ZERO) > 0) {
            return itemRate;
        }
        if (skuCode == null || unitCode == null || unitCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        log.warn("库存调拨单明细SKU[{}]单位[{}]的换算率为空，按1:1处理", skuCode, unitCode);
        return BigDecimal.ONE;
    }

    @Override
    public CommonResult<StockTransferMainDTO> getById(Long id) {
        StockTransferMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<StockTransferMainDTO> list(StockTransferQueryDTO queryDTO) {
        Page<StockTransferMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<StockTransferMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getTransferNo() != null) {
            wrapper.like(StockTransferMainEntity::getTransferNo, queryDTO.getTransferNo());
        }
        if (queryDTO.getFromWarehouseCode() != null) {
            wrapper.like(StockTransferMainEntity::getFromWarehouseCode, queryDTO.getFromWarehouseCode());
        }
        if (queryDTO.getToWarehouseCode() != null) {
            wrapper.like(StockTransferMainEntity::getToWarehouseCode, queryDTO.getToWarehouseCode());
        }
        if (queryDTO.getTransferDate() != null) {
            wrapper.eq(StockTransferMainEntity::getTransferDate, queryDTO.getTransferDate());
        }
        
        IPage<StockTransferMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<StockTransferMainDTO> save(StockTransferSaveDTO saveDTO) {
        StockTransferMainEntity entity = BeanUtils.convert(saveDTO, StockTransferMainEntity.class);
        if (entity.getTransferNo() == null || entity.getTransferNo().trim().isEmpty()) {
            entity.setTransferNo("STR" + IdUtils.generateId());
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        for (StockTransferItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            BigDecimal itemAmount = itemSaveDTO.getTransferQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        
        super.save(entity);
        syncSend(entity);
        
        List<StockTransferItemEntity> items = new ArrayList<>();
        for (StockTransferItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            StockTransferItemEntity item = BeanUtils.convert(itemSaveDTO, StockTransferItemEntity.class);
            item.setTransferId(entity.getId());
            item.setTransferNo(entity.getTransferNo());
            BigDecimal itemAmount = itemSaveDTO.getTransferQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }
        
        if (!items.isEmpty()) {
            batchUtils.saveBatch(stockTransferItemService, items);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<StockTransferMainDTO> update(Long id, StockTransferSaveDTO saveDTO) {
        StockTransferMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        for (StockTransferItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            BigDecimal itemAmount = itemSaveDTO.getTransferQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        
        super.updateById(entity);
        syncSend(entity);
        
        stockTransferItemService.remove(new LambdaQueryWrapper<StockTransferItemEntity>().eq(StockTransferItemEntity::getTransferId, id));
        
        List<StockTransferItemEntity> items = new ArrayList<>();
        for (StockTransferItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            StockTransferItemEntity item = BeanUtils.convert(itemSaveDTO, StockTransferItemEntity.class);
            item.setTransferId(entity.getId());
            item.setTransferNo(entity.getTransferNo());
            BigDecimal itemAmount = itemSaveDTO.getTransferQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }
        
        if (!items.isEmpty()) {
            batchUtils.saveBatch(stockTransferItemService, items);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        stockTransferItemService.remove(new LambdaQueryWrapper<StockTransferItemEntity>().eq(StockTransferItemEntity::getTransferId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        StockTransferMainEntity entity = super.getById(id);
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
    public CommonResult<Void> audit(Long id, Integer auditStatus) {
        StockTransferMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        if (auditStatus == 1) {
            entity.setStatus(2);
            List<StockTransferItemEntity> items = stockTransferItemService.list(
                    new LambdaQueryWrapper<StockTransferItemEntity>().eq(StockTransferItemEntity::getTransferId, id)
            );
            String fromWarehouseCode = entity.getFromWarehouseCode();
            String toWarehouseCode = entity.getToWarehouseCode();
            if (toWarehouseCode == null || toWarehouseCode.trim().isEmpty()) {
                throw new RuntimeException("调拨单缺少调入仓库编码");
            }
            for (StockTransferItemEntity item : items) {
                String skuCode = item.getSkuCode();
                if (skuCode == null || skuCode.trim().isEmpty()) {
                    log.warn("调拨单[{}]明细缺少skuCode，跳过调拨", entity.getTransferNo());
                    continue;
                }
                BigDecimal conversionRate = getConversionRate(skuCode, item.getUnit(), item.getConversionRate());
                BigDecimal stockQuantity = item.getTransferQuantity().multiply(conversionRate);
                CommonResult<Void> decreaseResult = stockService.decreaseStock(
                        fromWarehouseCode, skuCode, stockQuantity, entity.getTransferNo(), "STOCK_TRANSFER_OUT"
                );
                if (!decreaseResult.isSuccess()) {
                    throw new RuntimeException("调拨单调出仓库扣减库存失败: " + decreaseResult.getMessage());
                }
                String goodsCode = item.getGoodsCode();
                if (goodsCode == null || goodsCode.trim().isEmpty()) {
                    goodsCode = skuCode;
                }
                CommonResult<Void> increaseResult = stockService.increaseStock(
                        toWarehouseCode, goodsCode, skuCode, stockQuantity,
                        item.getUnitPrice(), entity.getTransferNo(), "STOCK_TRANSFER_IN"
                );
                if (!increaseResult.isSuccess()) {
                    throw new RuntimeException("调拨单调入仓库增加库存失败: " + increaseResult.getMessage());
                }
            }
        }
        super.updateById(entity);
        syncSend(entity);
        return CommonResult.success();
    }

    private StockTransferMainDTO convertToDTO(StockTransferMainEntity entity) {
        StockTransferMainDTO dto = BeanUtils.convert(entity, StockTransferMainDTO.class);
        
        List<StockTransferItemEntity> items = stockTransferItemService.list(
            new LambdaQueryWrapper<StockTransferItemEntity>().eq(StockTransferItemEntity::getTransferId, entity.getId())
        );
        
        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, StockTransferItemDTO.class)).toList());
        return dto;
    }
}
