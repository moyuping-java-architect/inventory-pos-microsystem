package com.psi.stock.service.impl;

import com.psi.stock.dto.StockLossItemDTO;
import com.psi.stock.dto.StockLossItemSaveDTO;
import com.psi.stock.dto.StockLossMainDTO;
import com.psi.stock.dto.StockLossQueryDTO;
import com.psi.stock.dto.StockLossSaveDTO;
import com.psi.stock.entity.StockLossItemEntity;
import com.psi.stock.entity.StockLossMainEntity;
import com.psi.stock.mapper.StockLossMainMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockLossItemService;
import com.psi.stock.service.StockLossMainService;
import com.psi.stock.service.StockService;
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
public class StockLossMainServiceImpl extends ServiceImpl<StockLossMainMapper, StockLossMainEntity> implements StockLossMainService {

    private final StockLossItemService stockLossItemService;
    private final BatchUtils batchUtils;
    private final StockService stockService;
    private final StockSyncProducer stockSyncProducer;

    public StockLossMainServiceImpl(StockLossItemService stockLossItemService, BatchUtils batchUtils,
                                    StockService stockService, StockSyncProducer stockSyncProducer) {
        this.stockLossItemService = stockLossItemService;
        this.batchUtils = batchUtils;
        this.stockService = stockService;
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockLossMainEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockLossMainEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockLossMainEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockLossMainEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockLossMainEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockLossMainEntity entity) {
        try {
            stockSyncProducer.sendStockLossMain(entity);
        } catch (Exception e) {
            log.error("报损主单实时同步发送失败", e);
        }
    }

    private BigDecimal getConversionRate(String skuCode, String unitCode, BigDecimal itemRate) {
        if (itemRate != null && itemRate.compareTo(BigDecimal.ZERO) > 0) {
            return itemRate;
        }
        if (skuCode == null || unitCode == null || unitCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        log.warn("库存报损单明细SKU[{}]单位[{}]的换算率为空，按1:1处理", skuCode, unitCode);
        return BigDecimal.ONE;
    }

    @Override
    public CommonResult<StockLossMainDTO> getById(Long id) {
        StockLossMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<StockLossMainDTO> list(StockLossQueryDTO queryDTO) {
        Page<StockLossMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<StockLossMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getLossNo() != null) {
            wrapper.like(StockLossMainEntity::getLossNo, queryDTO.getLossNo());
        }
        if (queryDTO.getWarehouseCode() != null) {
            wrapper.like(StockLossMainEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (queryDTO.getLossDate() != null) {
            wrapper.eq(StockLossMainEntity::getLossDate, queryDTO.getLossDate());
        }
        
        IPage<StockLossMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<StockLossMainDTO> save(StockLossSaveDTO saveDTO) {
        StockLossMainEntity entity = BeanUtils.convert(saveDTO, StockLossMainEntity.class);
        if (entity.getLossNo() == null || entity.getLossNo().trim().isEmpty()) {
            entity.setLossNo("SL" + IdUtils.generateId());
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        for (StockLossItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            BigDecimal itemAmount = itemSaveDTO.getLossQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        
        super.save(entity);
        syncSend(entity);
        
        List<StockLossItemEntity> items = new ArrayList<>();
        for (StockLossItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            StockLossItemEntity item = BeanUtils.convert(itemSaveDTO, StockLossItemEntity.class);
            item.setLossId(entity.getId());
            item.setLossNo(entity.getLossNo());
            BigDecimal itemAmount = itemSaveDTO.getLossQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }
        
        if (!items.isEmpty()) {
            batchUtils.saveBatch(stockLossItemService, items);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<StockLossMainDTO> update(Long id, StockLossSaveDTO saveDTO) {
        StockLossMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        for (StockLossItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            BigDecimal itemAmount = itemSaveDTO.getLossQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        
        super.updateById(entity);
        syncSend(entity);
        
        stockLossItemService.remove(new LambdaQueryWrapper<StockLossItemEntity>().eq(StockLossItemEntity::getLossId, id));
        
        List<StockLossItemEntity> items = new ArrayList<>();
        for (StockLossItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            StockLossItemEntity item = BeanUtils.convert(itemSaveDTO, StockLossItemEntity.class);
            item.setLossId(entity.getId());
            item.setLossNo(entity.getLossNo());
            BigDecimal itemAmount = itemSaveDTO.getLossQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }
        
        if (!items.isEmpty()) {
            batchUtils.saveBatch(stockLossItemService, items);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        stockLossItemService.remove(new LambdaQueryWrapper<StockLossItemEntity>().eq(StockLossItemEntity::getLossId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        StockLossMainEntity entity = super.getById(id);
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
        StockLossMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        if (auditStatus == 1) {
            entity.setStatus(2);
            List<StockLossItemEntity> items = stockLossItemService.list(
                    new LambdaQueryWrapper<StockLossItemEntity>().eq(StockLossItemEntity::getLossId, id)
            );
            for (StockLossItemEntity item : items) {
                String skuCode = item.getSkuCode();
                if (skuCode == null || skuCode.trim().isEmpty()) {
                    log.warn("报损单[{}]明细缺少skuCode，跳过库存扣减", entity.getLossNo());
                    continue;
                }
                BigDecimal conversionRate = getConversionRate(skuCode, item.getUnit(), item.getConversionRate());
                BigDecimal stockQuantity = item.getLossQuantity().multiply(conversionRate);
                CommonResult<Void> result = stockService.decreaseStock(
                        entity.getWarehouseCode(), skuCode, stockQuantity, entity.getLossNo(), "STOCK_LOSS"
                );
                if (!result.isSuccess()) {
                    throw new RuntimeException("报损单扣减库存失败: " + result.getMessage());
                }
            }
        }
        super.updateById(entity);
        syncSend(entity);
        return CommonResult.success();
    }

    private StockLossMainDTO convertToDTO(StockLossMainEntity entity) {
        StockLossMainDTO dto = BeanUtils.convert(entity, StockLossMainDTO.class);
        
        List<StockLossItemEntity> items = stockLossItemService.list(
            new LambdaQueryWrapper<StockLossItemEntity>().eq(StockLossItemEntity::getLossId, entity.getId())
        );
        
        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, StockLossItemDTO.class)).toList());
        return dto;
    }
}
