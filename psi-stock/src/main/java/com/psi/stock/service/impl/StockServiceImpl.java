package com.psi.stock.service.impl;

import com.psi.stock.client.StockGoClient;
import com.psi.stock.dto.StockBatchOperateItemDTO;
import com.psi.stock.dto.StockDTO;
import com.psi.stock.dto.StockQueryDTO;
import com.psi.stock.entity.StockEntity;
import com.psi.stock.mapper.StockMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.StockFlowService;
import com.psi.stock.service.StockGoCircuitService;
import com.psi.stock.service.StockService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, StockEntity> implements StockService {

    private final StockFlowService stockFlowService;
    private final StockGoClient stockGoClient;
    private final StockGoCircuitService stockGoCircuitService;
    private final StockSyncProducer stockSyncProducer;

    public StockServiceImpl(StockFlowService stockFlowService, StockGoClient stockGoClient,
                            StockGoCircuitService stockGoCircuitService, StockSyncProducer stockSyncProducer) {
        this.stockFlowService = stockFlowService;
        this.stockGoClient = stockGoClient;
        this.stockGoCircuitService = stockGoCircuitService;
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public CommonResult<StockDTO> getById(Long id) {
        StockEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, StockDTO.class));
    }

    @Override
    public PageResult<StockDTO> list(StockQueryDTO queryDTO) {
        Page<StockEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<StockEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getWarehouseCode() != null) {
            wrapper.like(StockEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (queryDTO.getGoodsCode() != null) {
            wrapper.like(StockEntity::getGoodsCode, queryDTO.getGoodsCode());
        }
        if (queryDTO.getSkuCode() != null) {
            wrapper.like(StockEntity::getSkuCode, queryDTO.getSkuCode());
        }
        if (queryDTO.getGoodsName() != null) {
            wrapper.like(StockEntity::getGoodsName, queryDTO.getGoodsName());
        }
        
        IPage<StockEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, StockDTO.class));
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        StockEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        return CommonResult.success();
    }

    @Override
    public boolean save(StockEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSendStock(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(StockEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSendStock(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(StockEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSendStock(entity);
        }
        return result;
    }

    private void syncSendStock(StockEntity entity) {
        try {
            stockSyncProducer.sendStock(entity);
        } catch (Exception e) {
            log.error("库存实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<StockDTO> getStock(String warehouseCode, String skuCode) {
        LambdaQueryWrapper<StockEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockEntity::getWarehouseCode, warehouseCode)
               .eq(StockEntity::getSkuCode, skuCode);
        
        StockEntity entity = super.getOne(wrapper);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, StockDTO.class));
    }

    @Override
    @Transactional
    public CommonResult<Void> increaseStock(String warehouseCode, String goodsCode, String skuCode, BigDecimal quantity,
                                             BigDecimal costPrice, String sourceNo, String sourceType) {
        if (stockGoClient.isEnabled()) {
            CommonResult<Void> result = stockGoCircuitService.increaseStock(warehouseCode, goodsCode, skuCode, quantity, costPrice, sourceNo, sourceType);
            if (!StockGoCircuitService.isCircuitOpen(result)) {
                return result;
            }
            log.warn("Go库存新增触发熔断降级，转本地Java实现: warehouseCode={}, skuCode={}", warehouseCode, skuCode);
        }
        LambdaQueryWrapper<StockEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockEntity::getWarehouseCode, warehouseCode)
               .eq(StockEntity::getSkuCode, skuCode);
        
        StockEntity entity = super.getOne(wrapper);
        
        BigDecimal beforeQuantity = BigDecimal.ZERO;
        BigDecimal afterQuantity;
        BigDecimal totalAmount;
        
        if (entity == null) {
            entity = new StockEntity();
            entity.setWarehouseCode(warehouseCode);
            entity.setGoodsCode(goodsCode);
            entity.setSkuCode(skuCode);
            entity.setQuantity(quantity);
            entity.setLockedQuantity(BigDecimal.ZERO);
            entity.setAvailableQuantity(quantity);
            entity.setAvgCostPrice(costPrice);
            entity.setTotalAmount(quantity.multiply(costPrice));
            super.save(entity);
        } else {
            beforeQuantity = entity.getQuantity();
            BigDecimal newQuantity = entity.getQuantity().add(quantity);
            BigDecimal totalCost = entity.getTotalAmount().add(quantity.multiply(costPrice));
            entity.setQuantity(newQuantity);
            entity.setAvailableQuantity(entity.getAvailableQuantity().add(quantity));
            entity.setAvgCostPrice(totalCost.divide(newQuantity, 4, BigDecimal.ROUND_HALF_UP));
            entity.setTotalAmount(totalCost);
            super.updateById(entity);
        }
        
        afterQuantity = entity.getQuantity();
        totalAmount = quantity.multiply(costPrice);
        
        stockFlowService.addFlow(warehouseCode, null, goodsCode, skuCode, null, null, null, 
                                1, quantity, BigDecimal.ZERO, beforeQuantity, afterQuantity,
                                costPrice, totalAmount, sourceNo, sourceType, "入库");
        
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> decreaseStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                             String sourceNo, String sourceType) {
        if (stockGoClient.isEnabled()) {
            CommonResult<Void> result = stockGoCircuitService.decreaseStock(warehouseCode, skuCode, quantity, sourceNo, sourceType);
            if (!StockGoCircuitService.isCircuitOpen(result)) {
                return result;
            }
            log.warn("Go库存扣减触发熔断降级，转本地Java实现: warehouseCode={}, skuCode={}", warehouseCode, skuCode);
        }
        // 先查询当前库存，用于记录流水（非锁定读，流水数量可能略有偏差但可接受）
        LambdaQueryWrapper<StockEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StockEntity::getWarehouseCode, warehouseCode)
                    .eq(StockEntity::getSkuCode, skuCode);
        StockEntity entity = super.getOne(queryWrapper);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        // CAS 原子扣减：只有 available_quantity >= quantity 时才更新成功
        LambdaUpdateWrapper<StockEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StockEntity::getWarehouseCode, warehouseCode)
                     .eq(StockEntity::getSkuCode, skuCode)
                     .ge(StockEntity::getAvailableQuantity, quantity);
        updateWrapper.setSql("quantity = quantity - {0}", quantity)
                     .setSql("available_quantity = available_quantity - {0}", quantity)
                     .setSql("total_amount = total_amount - (avg_cost_price * {0})", quantity);
        
        boolean success = super.update(updateWrapper);
        if (!success) {
            log.warn("库存扣减失败，库存不足或并发冲突: warehouseCode=" + warehouseCode
                    + ", skuCode=" + skuCode + ", quantity=" + quantity);
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "库存不足");
        }
        
        BigDecimal beforeQuantity = entity.getQuantity();
        BigDecimal afterQuantity = entity.getQuantity().subtract(quantity);
        BigDecimal amount = quantity.multiply(entity.getAvgCostPrice());
        
        stockFlowService.addFlow(warehouseCode, null, entity.getGoodsCode(), skuCode, null, null, null,
                                2, BigDecimal.ZERO, quantity, beforeQuantity, afterQuantity,
                                entity.getAvgCostPrice(), amount, sourceNo, sourceType, "出库");
        
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> lockStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                         String sourceNo, String sourceType) {
        if (stockGoClient.isEnabled()) {
            CommonResult<Void> result = stockGoCircuitService.lockStock(warehouseCode, skuCode, quantity, sourceNo, sourceType);
            if (!StockGoCircuitService.isCircuitOpen(result)) {
                return result;
            }
            log.warn("Go库存预占触发熔断降级，转本地Java实现: warehouseCode={}, skuCode={}", warehouseCode, skuCode);
        }
        LambdaUpdateWrapper<StockEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StockEntity::getWarehouseCode, warehouseCode)
                     .eq(StockEntity::getSkuCode, skuCode)
                     .ge(StockEntity::getAvailableQuantity, quantity);
        updateWrapper.setSql("available_quantity = available_quantity - {0}", quantity)
                     .setSql("locked_quantity = locked_quantity + {0}", quantity);
        
        boolean success = super.update(updateWrapper);
        if (!success) {
            log.warn("库存预占失败，可用库存不足或并发冲突: warehouseCode=" + warehouseCode
                    + ", skuCode=" + skuCode + ", quantity=" + quantity);
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "库存不足");
        }
        
        StockEntity entity = super.getOne(new LambdaQueryWrapper<StockEntity>()
                .eq(StockEntity::getWarehouseCode, warehouseCode)
                .eq(StockEntity::getSkuCode, skuCode));
        
        stockFlowService.addFlow(warehouseCode, null, entity.getGoodsCode(), skuCode, null, null, null,
                                3, BigDecimal.ZERO, quantity, null, null,
                                entity.getAvgCostPrice(), BigDecimal.ZERO, sourceNo, sourceType, "预占");
        
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> releaseStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                            String sourceNo, String sourceType) {
        if (stockGoClient.isEnabled()) {
            CommonResult<Void> result = stockGoCircuitService.releaseStock(warehouseCode, skuCode, quantity, sourceNo, sourceType);
            if (!StockGoCircuitService.isCircuitOpen(result)) {
                return result;
            }
            log.warn("Go库存释放触发熔断降级，转本地Java实现: warehouseCode={}, skuCode={}", warehouseCode, skuCode);
        }
        LambdaUpdateWrapper<StockEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StockEntity::getWarehouseCode, warehouseCode)
                     .eq(StockEntity::getSkuCode, skuCode)
                     .ge(StockEntity::getLockedQuantity, quantity);
        updateWrapper.setSql("available_quantity = available_quantity + {0}", quantity)
                     .setSql("locked_quantity = locked_quantity - {0}", quantity);
        
        boolean success = super.update(updateWrapper);
        if (!success) {
            log.warn("库存释放失败，预占库存不足或并发冲突: warehouseCode=" + warehouseCode
                    + ", skuCode=" + skuCode + ", quantity=" + quantity);
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "预占库存不足");
        }
        
        StockEntity entity = super.getOne(new LambdaQueryWrapper<StockEntity>()
                .eq(StockEntity::getWarehouseCode, warehouseCode)
                .eq(StockEntity::getSkuCode, skuCode));
        
        stockFlowService.addFlow(warehouseCode, null, entity.getGoodsCode(), skuCode, null, null, null,
                                4, quantity, BigDecimal.ZERO, null, null,
                                entity.getAvgCostPrice(), BigDecimal.ZERO, sourceNo, sourceType, "释放");
        
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> confirmStock(String warehouseCode, String skuCode, BigDecimal quantity,
                                            String sourceNo, String sourceType) {
        if (stockGoClient.isEnabled()) {
            CommonResult<Void> result = stockGoCircuitService.confirmStock(warehouseCode, skuCode, quantity, sourceNo, sourceType);
            if (!StockGoCircuitService.isCircuitOpen(result)) {
                return result;
            }
            log.warn("Go库存确认出库触发熔断降级，转本地Java实现: warehouseCode={}, skuCode={}", warehouseCode, skuCode);
        }
        LambdaUpdateWrapper<StockEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StockEntity::getWarehouseCode, warehouseCode)
                     .eq(StockEntity::getSkuCode, skuCode)
                     .ge(StockEntity::getLockedQuantity, quantity);
        updateWrapper.setSql("quantity = quantity - {0}", quantity)
                     .setSql("locked_quantity = locked_quantity - {0}", quantity)
                     .setSql("total_amount = total_amount - (avg_cost_price * {0})", quantity);
        
        boolean success = super.update(updateWrapper);
        if (!success) {
            log.warn("库存确认出库失败，预占库存不足或并发冲突: warehouseCode=" + warehouseCode
                    + ", skuCode=" + skuCode + ", quantity=" + quantity);
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "预占库存不足");
        }
        
        StockEntity entity = super.getOne(new LambdaQueryWrapper<StockEntity>()
                .eq(StockEntity::getWarehouseCode, warehouseCode)
                .eq(StockEntity::getSkuCode, skuCode));
        
        stockFlowService.addFlow(warehouseCode, null, entity.getGoodsCode(), skuCode, null, null, null,
                                5, BigDecimal.ZERO, quantity, entity.getQuantity().add(quantity), entity.getQuantity(),
                                entity.getAvgCostPrice(), quantity.multiply(entity.getAvgCostPrice()), sourceNo, sourceType, "确认出库");
        
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> batchLockStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType) {
        if (items == null || items.isEmpty()) {
            return CommonResult.success();
        }

        // 防止同一 SKU 重复扣减导致数量错误
        long distinctCount = items.stream()
                .map(i -> i.getWarehouseCode() + "#" + i.getSkuCode())
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "批量预占库存存在重复SKU");
        }

        int affectedRows = baseMapper.batchLockStock(items);
        if (affectedRows != items.size()) {
            log.warn("批量预占库存失败，库存不足或SKU不存在: sourceNo=" + sourceNo
                    + ", items=" + items.size() + ", affectedRows=" + affectedRows);
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "库存不足");
        }

        // 查询更新后库存，记录流水
        for (StockBatchOperateItemDTO item : items) {
            StockEntity entity = super.getOne(new LambdaQueryWrapper<StockEntity>()
                    .eq(StockEntity::getWarehouseCode, item.getWarehouseCode())
                    .eq(StockEntity::getSkuCode, item.getSkuCode()));
            if (entity != null) {
                stockFlowService.addFlow(item.getWarehouseCode(), null, entity.getGoodsCode(), item.getSkuCode(),
                        null, null, null, 3, BigDecimal.ZERO, item.getQuantity(),
                        null, null, entity.getAvgCostPrice(), BigDecimal.ZERO,
                        sourceNo, sourceType, "批量预占");
            }
        }

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> batchDecreaseStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType) {
        if (items == null || items.isEmpty()) {
            return CommonResult.success();
        }
        if (stockGoClient.isEnabled()) {
            CommonResult<Void> result = stockGoCircuitService.batchDecreaseStock(items, sourceNo, sourceType);
            if (!StockGoCircuitService.isCircuitOpen(result)) {
                return result;
            }
            log.warn("Go批量库存扣减触发熔断降级，转本地Java实现: items={}", items.size());
        }

        // 防止同一 SKU 重复扣减
        long distinctCount = items.stream()
                .map(i -> i.getWarehouseCode() + "#" + i.getSkuCode())
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "批量扣减库存存在重复SKU");
        }

        // 批量查询更新前库存，用于记录流水
        Map<String, StockEntity> beforeMap = items.stream()
                .map(item -> super.getOne(new LambdaQueryWrapper<StockEntity>()
                        .eq(StockEntity::getWarehouseCode, item.getWarehouseCode())
                        .eq(StockEntity::getSkuCode, item.getSkuCode())))
                .filter(e -> e != null)
                .collect(Collectors.toMap(e -> e.getWarehouseCode() + "#" + e.getSkuCode(), e -> e, (a, b) -> a));

        int affectedRows = baseMapper.batchDecreaseStock(items);
        if (affectedRows != items.size()) {
            log.warn("批量扣减库存失败，库存不足或SKU不存在: sourceNo=" + sourceNo
                    + ", items=" + items.size() + ", affectedRows=" + affectedRows);
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "库存不足");
        }

        // 记录流水
        for (StockBatchOperateItemDTO item : items) {
            StockEntity before = beforeMap.get(item.getWarehouseCode() + "#" + item.getSkuCode());
            BigDecimal beforeQuantity = before != null ? before.getQuantity() : BigDecimal.ZERO;
            BigDecimal afterQuantity = beforeQuantity.subtract(item.getQuantity());
            BigDecimal costPrice = before != null ? before.getAvgCostPrice() : BigDecimal.ZERO;
            BigDecimal amount = item.getQuantity().multiply(costPrice);

            stockFlowService.addFlow(item.getWarehouseCode(), null,
                    before != null ? before.getGoodsCode() : null, item.getSkuCode(),
                    null, null, null, 2, BigDecimal.ZERO, item.getQuantity(),
                    beforeQuantity, afterQuantity, costPrice, amount,
                    sourceNo, sourceType, "批量出库");
        }

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> batchReleaseStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType) {
        if (items == null || items.isEmpty()) {
            return CommonResult.success();
        }

        long distinctCount = items.stream()
                .map(i -> i.getWarehouseCode() + "#" + i.getSkuCode())
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "批量释放库存存在重复SKU");
        }

        int affectedRows = baseMapper.batchReleaseStock(items);
        if (affectedRows != items.size()) {
            log.warn("批量释放库存失败，预占库存不足或SKU不存在: sourceNo=" + sourceNo
                    + ", items=" + items.size() + ", affectedRows=" + affectedRows);
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "预占库存不足");
        }

        for (StockBatchOperateItemDTO item : items) {
            StockEntity entity = super.getOne(new LambdaQueryWrapper<StockEntity>()
                    .eq(StockEntity::getWarehouseCode, item.getWarehouseCode())
                    .eq(StockEntity::getSkuCode, item.getSkuCode()));
            if (entity != null) {
                stockFlowService.addFlow(item.getWarehouseCode(), null, entity.getGoodsCode(), item.getSkuCode(),
                        null, null, null, 4, item.getQuantity(), BigDecimal.ZERO,
                        null, null, entity.getAvgCostPrice(), BigDecimal.ZERO,
                        sourceNo, sourceType, "批量释放");
            }
        }

        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> batchConfirmStock(List<StockBatchOperateItemDTO> items, String sourceNo, String sourceType) {
        if (items == null || items.isEmpty()) {
            return CommonResult.success();
        }

        long distinctCount = items.stream()
                .map(i -> i.getWarehouseCode() + "#" + i.getSkuCode())
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "批量确认出库存在重复SKU");
        }

        Map<String, StockEntity> beforeMap = items.stream()
                .map(item -> super.getOne(new LambdaQueryWrapper<StockEntity>()
                        .eq(StockEntity::getWarehouseCode, item.getWarehouseCode())
                        .eq(StockEntity::getSkuCode, item.getSkuCode())))
                .filter(e -> e != null)
                .collect(Collectors.toMap(e -> e.getWarehouseCode() + "#" + e.getSkuCode(), e -> e, (a, b) -> a));

        int affectedRows = baseMapper.batchConfirmStock(items);
        if (affectedRows != items.size()) {
            log.warn("批量确认出库失败，预占库存不足或SKU不存在: sourceNo=" + sourceNo
                    + ", items=" + items.size() + ", affectedRows=" + affectedRows);
            return CommonResult.fail(ResultCode.BUSINESS_ERROR, "预占库存不足");
        }

        for (StockBatchOperateItemDTO item : items) {
            StockEntity before = beforeMap.get(item.getWarehouseCode() + "#" + item.getSkuCode());
            BigDecimal beforeQuantity = before != null ? before.getQuantity() : BigDecimal.ZERO;
            BigDecimal afterQuantity = beforeQuantity.subtract(item.getQuantity());
            BigDecimal costPrice = before != null ? before.getAvgCostPrice() : BigDecimal.ZERO;
            BigDecimal amount = item.getQuantity().multiply(costPrice);

            stockFlowService.addFlow(item.getWarehouseCode(), null,
                    before != null ? before.getGoodsCode() : null, item.getSkuCode(),
                    null, null, null, 5, BigDecimal.ZERO, item.getQuantity(),
                    beforeQuantity, afterQuantity, costPrice, amount,
                    sourceNo, sourceType, "批量确认出库");
        }

        return CommonResult.success();
    }
}