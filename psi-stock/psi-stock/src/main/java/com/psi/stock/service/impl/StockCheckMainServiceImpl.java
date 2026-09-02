package com.psi.stock.service.impl;

import com.psi.stock.dto.StockCheckItemDTO;
import com.psi.stock.dto.StockCheckItemSaveDTO;
import com.psi.stock.dto.StockCheckMainDTO;
import com.psi.stock.dto.StockCheckQueryDTO;
import com.psi.stock.dto.StockCheckSaveDTO;
import com.psi.stock.dto.StockDTO;
import com.psi.stock.entity.StockCheckItemEntity;
import com.psi.stock.entity.StockCheckMainEntity;
import com.psi.stock.mapper.StockCheckMainMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockCheckItemService;
import com.psi.stock.service.StockCheckMainService;
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
public class StockCheckMainServiceImpl extends ServiceImpl<StockCheckMainMapper, StockCheckMainEntity> implements StockCheckMainService {

    private final StockCheckItemService stockCheckItemService;
    private final BatchUtils batchUtils;
    private final StockService stockService;
    private final StockSyncProducer stockSyncProducer;

    public StockCheckMainServiceImpl(StockCheckItemService stockCheckItemService, BatchUtils batchUtils,
                                     StockService stockService, StockSyncProducer stockSyncProducer) {
        this.stockCheckItemService = stockCheckItemService;
        this.batchUtils = batchUtils;
        this.stockService = stockService;
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(StockCheckMainEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockCheckMainEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockCheckMainEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<StockCheckMainEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (StockCheckMainEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(StockCheckMainEntity entity) {
        try {
            stockSyncProducer.sendStockCheckMain(entity);
        } catch (Exception e) {
            log.error("盘点主单实时同步发送失败", e);
        }
    }

    private BigDecimal getConversionRate(String skuCode, String unitCode, BigDecimal itemRate) {
        if (itemRate != null && itemRate.compareTo(BigDecimal.ZERO) > 0) {
            return itemRate;
        }
        if (skuCode == null || unitCode == null || unitCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        log.warn("库存盘点单明细SKU[{}]单位[{}]的换算率为空，按1:1处理", skuCode, unitCode);
        return BigDecimal.ONE;
    }

    private BigDecimal getCurrentStockQuantity(String warehouseCode, String skuCode) {
        if (skuCode == null) {
            return BigDecimal.ZERO;
        }
        CommonResult<StockDTO> result = stockService.getStock(warehouseCode, skuCode);
        if (result.isSuccess() && result.getData() != null) {
            return result.getData().getQuantity();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public CommonResult<StockCheckMainDTO> getById(Long id) {
        StockCheckMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<StockCheckMainDTO> list(StockCheckQueryDTO queryDTO) {
        Page<StockCheckMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<StockCheckMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getCheckNo() != null) {
            wrapper.like(StockCheckMainEntity::getCheckNo, queryDTO.getCheckNo());
        }
        if (queryDTO.getWarehouseCode() != null) {
            wrapper.like(StockCheckMainEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (queryDTO.getCheckDate() != null) {
            wrapper.eq(StockCheckMainEntity::getCheckDate, queryDTO.getCheckDate());
        }
        
        IPage<StockCheckMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<StockCheckMainDTO> save(StockCheckSaveDTO saveDTO) {
        StockCheckMainEntity entity = BeanUtils.convert(saveDTO, StockCheckMainEntity.class);
        if (entity.getCheckNo() == null || entity.getCheckNo().trim().isEmpty()) {
            entity.setCheckNo("SCK" + IdUtils.generateId());
        }
        entity.setStatus(1);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal diffAmount = BigDecimal.ZERO;
        String warehouseCode = entity.getWarehouseCode();
        
        super.save(entity);
        syncSend(entity);
        
        List<StockCheckItemEntity> items = new ArrayList<>();
        for (StockCheckItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            StockCheckItemEntity item = BeanUtils.convert(itemSaveDTO, StockCheckItemEntity.class);
            item.setCheckId(entity.getId());
            item.setCheckNo(entity.getCheckNo());
            
            BigDecimal actualQuantity = itemSaveDTO.getActualQuantity() != null ? itemSaveDTO.getActualQuantity() : BigDecimal.ZERO;
            BigDecimal bookQuantity = getCurrentStockQuantity(warehouseCode, item.getSkuCode());
            BigDecimal conversionRate = getConversionRate(item.getSkuCode(), itemSaveDTO.getUnit(), itemSaveDTO.getConversionRate());
            BigDecimal actualStockQuantity = actualQuantity.multiply(conversionRate);
            BigDecimal diffQuantity = actualStockQuantity.subtract(bookQuantity);
            
            BigDecimal unitPrice = itemSaveDTO.getUnitPrice() != null ? itemSaveDTO.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal bookAmount = bookQuantity.multiply(unitPrice);
            BigDecimal actualAmount = actualStockQuantity.multiply(unitPrice);
            
            item.setBookQuantity(bookQuantity);
            item.setActualQuantity(actualQuantity);
            item.setDiffQuantity(diffQuantity);
            item.setBookAmount(bookAmount);
            item.setActualAmount(actualAmount);
            item.setDiffAmount(actualAmount.subtract(bookAmount));
            items.add(item);
            
            totalAmount = totalAmount.add(bookAmount);
            diffAmount = diffAmount.add(actualAmount.subtract(bookAmount));
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setDiffAmount(diffAmount);
        super.updateById(entity);
        syncSend(entity);
        
        if (!items.isEmpty()) {
            batchUtils.saveBatch(stockCheckItemService, items);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<StockCheckMainDTO> update(Long id, StockCheckSaveDTO saveDTO) {
        StockCheckMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal diffAmount = BigDecimal.ZERO;
        
        for (StockCheckItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            BigDecimal bookAmount = itemSaveDTO.getBookQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal actualAmount = itemSaveDTO.getActualQuantity().multiply(itemSaveDTO.getUnitPrice());
            
            totalAmount = totalAmount.add(bookAmount);
            diffAmount = diffAmount.add(actualAmount.subtract(bookAmount));
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setDiffAmount(diffAmount);
        
        super.updateById(entity);
        syncSend(entity);
        
        stockCheckItemService.remove(new LambdaQueryWrapper<StockCheckItemEntity>().eq(StockCheckItemEntity::getCheckId, id));
        
        List<StockCheckItemEntity> items = new ArrayList<>();
        for (StockCheckItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            StockCheckItemEntity item = BeanUtils.convert(itemSaveDTO, StockCheckItemEntity.class);
            item.setCheckId(entity.getId());
            item.setCheckNo(entity.getCheckNo());
            
            BigDecimal bookAmount = itemSaveDTO.getBookQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal actualAmount = itemSaveDTO.getActualQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal diffQuantity = itemSaveDTO.getActualQuantity().subtract(itemSaveDTO.getBookQuantity());
            
            item.setDiffQuantity(diffQuantity);
            item.setBookAmount(bookAmount);
            item.setActualAmount(actualAmount);
            item.setDiffAmount(actualAmount.subtract(bookAmount));
            items.add(item);
        }
        
        if (!items.isEmpty()) {
            batchUtils.saveBatch(stockCheckItemService, items);
        }
        
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        stockCheckItemService.remove(new LambdaQueryWrapper<StockCheckItemEntity>().eq(StockCheckItemEntity::getCheckId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        StockCheckMainEntity entity = super.getById(id);
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
        StockCheckMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        if (auditStatus == 1) {
            entity.setStatus(2);
            List<StockCheckItemEntity> items = stockCheckItemService.list(
                    new LambdaQueryWrapper<StockCheckItemEntity>().eq(StockCheckItemEntity::getCheckId, id)
            );
            for (StockCheckItemEntity item : items) {
                String skuCode = item.getSkuCode();
                if (skuCode == null || skuCode.trim().isEmpty()) {
                    log.warn("盘点单[{}]明细缺少skuCode，跳过库存调整", entity.getCheckNo());
                    continue;
                }
                BigDecimal diff = item.getDiffQuantity();
                if (diff == null || diff.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    String goodsCode = item.getGoodsCode();
                    if (goodsCode == null || goodsCode.trim().isEmpty()) {
                        goodsCode = skuCode;
                    }
                    CommonResult<Void> result = stockService.increaseStock(
                            entity.getWarehouseCode(), goodsCode, skuCode, diff,
                            item.getUnitPrice(), entity.getCheckNo(), "STOCK_CHECK"
                    );
                    if (!result.isSuccess()) {
                        throw new RuntimeException("盘点单盘盈增加库存失败: " + result.getMessage());
                    }
                } else {
                    CommonResult<Void> result = stockService.decreaseStock(
                            entity.getWarehouseCode(), skuCode, diff.abs(), entity.getCheckNo(), "STOCK_CHECK"
                    );
                    if (!result.isSuccess()) {
                        throw new RuntimeException("盘点单盘亏扣减库存失败: " + result.getMessage());
                    }
                }
            }
        }
        super.updateById(entity);
        syncSend(entity);
        return CommonResult.success();
    }

    private StockCheckMainDTO convertToDTO(StockCheckMainEntity entity) {
        StockCheckMainDTO dto = BeanUtils.convert(entity, StockCheckMainDTO.class);
        
        List<StockCheckItemEntity> items = stockCheckItemService.list(
            new LambdaQueryWrapper<StockCheckItemEntity>().eq(StockCheckItemEntity::getCheckId, entity.getId())
        );
        
        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, StockCheckItemDTO.class)).toList());
        return dto;
    }
}
