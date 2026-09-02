package com.psi.stock.service.impl;

import com.psi.stock.dto.StockOverItemDTO;
import com.psi.stock.dto.StockOverItemSaveDTO;
import com.psi.stock.dto.StockOverMainDTO;
import com.psi.stock.dto.StockOverQueryDTO;
import com.psi.stock.dto.StockOverSaveDTO;
import com.psi.stock.entity.StockOverItemEntity;
import com.psi.stock.entity.StockOverMainEntity;
import com.psi.stock.mapper.StockOverMainMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockOverItemService;
import com.psi.stock.service.StockOverMainService;
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
public class StockOverMainServiceImpl extends ServiceImpl<StockOverMainMapper, StockOverMainEntity> implements StockOverMainService {

    private final StockOverItemService stockOverItemService;
    private final BatchUtils batchUtils;
    private final StockService stockService;
    private final StockSyncProducer stockSyncProducer;

    public StockOverMainServiceImpl(StockOverItemService stockOverItemService, BatchUtils batchUtils,
                                    StockService stockService, StockSyncProducer stockSyncProducer) {
        this.stockOverItemService = stockOverItemService;
        this.batchUtils = batchUtils;
        this.stockService = stockService;
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockOverMainEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockOverMainEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockOverMainEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockOverMainEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockOverMainEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockOverMainEntity entity) {
        try {
            stockSyncProducer.sendStockOverMain(entity);
        } catch (Exception e) {
            log.error("报溢主单实时同步发送失败", e);
        }
    }

    private BigDecimal getConversionRate(String skuCode, String unitCode, BigDecimal itemRate) {
        if (itemRate != null && itemRate.compareTo(BigDecimal.ZERO) > 0) {
            return itemRate;
        }
        if (skuCode == null || unitCode == null || unitCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        log.warn("库存报溢单明细SKU[{}]单位[{}]的换算率为空，按1:1处理", skuCode, unitCode);
        return BigDecimal.ONE;
    }

    @Override
    public CommonResult<StockOverMainDTO> getById(Long id) {
        StockOverMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<StockOverMainDTO> list(StockOverQueryDTO queryDTO) {
        Page<StockOverMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<StockOverMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getOverNo() != null) {
            wrapper.like(StockOverMainEntity::getOverNo, queryDTO.getOverNo());
        }
        if (queryDTO.getWarehouseCode() != null) {
            wrapper.like(StockOverMainEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (queryDTO.getOverDate() != null) {
            wrapper.eq(StockOverMainEntity::getOverDate, queryDTO.getOverDate());
        }
        
        IPage<StockOverMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<StockOverMainDTO> save(StockOverSaveDTO saveDTO) {
        StockOverMainEntity entity = BeanUtils.convert(saveDTO, StockOverMainEntity.class);
        if (entity.getOverNo() == null || entity.getOverNo().trim().isEmpty()) {
            entity.setOverNo("SOV" + IdUtils.generateId());
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        for (StockOverItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            BigDecimal itemAmount = itemSaveDTO.getOverQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        
        super.save(entity);
        syncSend(entity);
        
        List<StockOverItemEntity> items = new ArrayList<>();
        for (StockOverItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            StockOverItemEntity item = BeanUtils.convert(itemSaveDTO, StockOverItemEntity.class);
            item.setOverId(entity.getId());
            item.setOverNo(entity.getOverNo());
            BigDecimal itemAmount = itemSaveDTO.getOverQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }
        
        if (!items.isEmpty()) {
            batchUtils.saveBatch(stockOverItemService, items);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<StockOverMainDTO> update(Long id, StockOverSaveDTO saveDTO) {
        StockOverMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        for (StockOverItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            BigDecimal itemAmount = itemSaveDTO.getOverQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        
        super.updateById(entity);
        syncSend(entity);
        
        stockOverItemService.remove(new LambdaQueryWrapper<StockOverItemEntity>().eq(StockOverItemEntity::getOverId, id));
        
        List<StockOverItemEntity> items = new ArrayList<>();
        for (StockOverItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            StockOverItemEntity item = BeanUtils.convert(itemSaveDTO, StockOverItemEntity.class);
            item.setOverId(entity.getId());
            item.setOverNo(entity.getOverNo());
            BigDecimal itemAmount = itemSaveDTO.getOverQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());
            
            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }
        
        if (!items.isEmpty()) {
            batchUtils.saveBatch(stockOverItemService, items);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        stockOverItemService.remove(new LambdaQueryWrapper<StockOverItemEntity>().eq(StockOverItemEntity::getOverId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        StockOverMainEntity entity = super.getById(id);
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
        StockOverMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        if (auditStatus == 1) {
            entity.setStatus(2);
            List<StockOverItemEntity> items = stockOverItemService.list(
                    new LambdaQueryWrapper<StockOverItemEntity>().eq(StockOverItemEntity::getOverId, id)
            );
            for (StockOverItemEntity item : items) {
                String skuCode = item.getSkuCode();
                if (skuCode == null || skuCode.trim().isEmpty()) {
                    log.warn("报溢单[{}]明细缺少skuCode，跳过库存增加", entity.getOverNo());
                    continue;
                }
                String goodsCode = item.getGoodsCode();
                if (goodsCode == null || goodsCode.trim().isEmpty()) {
                    goodsCode = skuCode;
                }
                BigDecimal conversionRate = getConversionRate(skuCode, item.getUnit(), item.getConversionRate());
                BigDecimal stockQuantity = item.getOverQuantity().multiply(conversionRate);
                CommonResult<Void> result = stockService.increaseStock(
                        entity.getWarehouseCode(), goodsCode, skuCode, stockQuantity,
                        item.getUnitPrice(), entity.getOverNo(), "STOCK_OVER"
                );
                if (!result.isSuccess()) {
                    throw new RuntimeException("报溢单增加库存失败: " + result.getMessage());
                }
            }
        }
        super.updateById(entity);
        syncSend(entity);
        return CommonResult.success();
    }

    private StockOverMainDTO convertToDTO(StockOverMainEntity entity) {
        StockOverMainDTO dto = BeanUtils.convert(entity, StockOverMainDTO.class);
        
        List<StockOverItemEntity> items = stockOverItemService.list(
            new LambdaQueryWrapper<StockOverItemEntity>().eq(StockOverItemEntity::getOverId, entity.getId())
        );
        
        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, StockOverItemDTO.class)).toList());
        return dto;
    }
}
