package com.psi.stock.service.impl;

import com.psi.stock.dto.InventoryInitItemSaveDTO;
import com.psi.stock.dto.InventoryInitSaveDTO;
import com.psi.stock.entity.InventoryInitItemEntity;
import com.psi.stock.entity.InventoryInitMainEntity;
import com.psi.stock.entity.StockEntity;
import com.psi.stock.mapper.InventoryInitMainMapper;
import com.psi.stock.mapper.StockMapper;
import com.psi.stock.mq.producer.StockSyncProducer;
import com.psi.stock.service.InventoryInitItemService;
import com.psi.stock.service.InventoryInitMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.ResultCode;
import com.psi.common.mybatis.util.BatchUtils;
import com.psi.common.util.BeanUtils;
import com.psi.common.util.IdUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
public class InventoryInitMainServiceImpl extends ServiceImpl<InventoryInitMainMapper, InventoryInitMainEntity> implements InventoryInitMainService {

    private final InventoryInitItemService inventoryInitItemService;
    private final BatchUtils batchUtils;
    private final StockMapper stockMapper;
    private final StockSyncProducer stockSyncProducer;

    public InventoryInitMainServiceImpl(InventoryInitItemService inventoryInitItemService, BatchUtils batchUtils,
                                        StockMapper stockMapper, StockSyncProducer stockSyncProducer) {
        this.inventoryInitItemService = inventoryInitItemService;
        this.batchUtils = batchUtils;
        this.stockMapper = stockMapper;
        this.stockSyncProducer = stockSyncProducer;
    }

    @Override
    public boolean save(InventoryInitMainEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(InventoryInitMainEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(InventoryInitMainEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            syncSend(entity);
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<InventoryInitMainEntity> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            for (InventoryInitMainEntity entity : entityList) {
                syncSend(entity);
            }
        }
        return result;
    }

    private void syncSend(InventoryInitMainEntity entity) {
        try {
            stockSyncProducer.sendInventoryInitMain(entity);
        } catch (Exception e) {
            log.error("库存初始化主单实时同步发送失败", e);
        }
    }

    private BigDecimal getConversionRate(String skuCode, String unitCode, BigDecimal itemRate) {
        if (itemRate != null && itemRate.compareTo(BigDecimal.ZERO) > 0) {
            return itemRate;
        }
        if (skuCode == null || unitCode == null || unitCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        log.warn("库存初始化单明细SKU[{}]单位[{}]的换算率为空，按1:1处理", skuCode, unitCode);
        return BigDecimal.ONE;
    }

    @Override
    @Transactional
    public CommonResult<Long> save(InventoryInitSaveDTO saveDTO) {
        InventoryInitMainEntity entity = BeanUtils.convert(saveDTO, InventoryInitMainEntity.class);
        if (entity.getInitNo() == null || entity.getInitNo().trim().isEmpty()) {
            entity.setInitNo("INIT" + IdUtils.generateId());
        }
        entity.setTotalAmount(BigDecimal.ZERO);
        super.save(entity);
        syncSend(entity);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InventoryInitItemEntity> items = new ArrayList<>();
        if (saveDTO.getItems() != null && !saveDTO.getItems().isEmpty()) {
            for (InventoryInitItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
                InventoryInitItemEntity item = BeanUtils.convert(itemSaveDTO, InventoryInitItemEntity.class);
                item.setInitId(entity.getId());
                item.setInitNo(entity.getInitNo());
                BigDecimal itemAmount = itemSaveDTO.getInitQuantity().multiply(itemSaveDTO.getUnitPrice());
                item.setAmount(itemAmount);
                item.setTaxAmount(BigDecimal.ZERO);
                totalAmount = totalAmount.add(itemAmount);
                items.add(item);
            }
        }

        if (!items.isEmpty()) {
            batchUtils.saveBatch(inventoryInitItemService, items);
        }

        entity.setTotalAmount(totalAmount);
        super.updateById(entity);
        syncSend(entity);

        return CommonResult.success(entity.getId());
    }

    @Override
    @Transactional
    public CommonResult<Void> audit(Long id, Integer auditStatus) {
        InventoryInitMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        if (auditStatus == 1) {
            entity.setOrderStatus(2);
            List<InventoryInitItemEntity> items = inventoryInitItemService.list(
                    new LambdaQueryWrapper<InventoryInitItemEntity>().eq(InventoryInitItemEntity::getInitId, id)
            );
            String warehouseCode = entity.getWarehouseCode();
            for (InventoryInitItemEntity item : items) {
                String skuCode = item.getSkuCode();
                if (skuCode == null || skuCode.trim().isEmpty()) {
                    log.warn("库存初始化单[{}]明细缺少skuCode，跳过", entity.getInitNo());
                    continue;
                }
                BigDecimal conversionRate = getConversionRate(skuCode, item.getUnit(), item.getConversionRate());
                BigDecimal stockQuantity = item.getInitQuantity().multiply(conversionRate);
                String goodsCode = item.getGoodsCode();
                if (goodsCode == null || goodsCode.trim().isEmpty()) {
                    goodsCode = skuCode;
                }

                LambdaQueryWrapper<StockEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(StockEntity::getWarehouseCode, warehouseCode).eq(StockEntity::getSkuCode, skuCode);
                StockEntity stock = stockMapper.selectOne(wrapper);
                if (stock == null) {
                    stock = new StockEntity();
                    stock.setDataUuid(java.util.UUID.randomUUID().toString());
                    stock.setWarehouseCode(warehouseCode);
                    stock.setWarehouseName(entity.getWarehouseName());
                    stock.setGoodsCode(goodsCode);
                    stock.setSkuCode(skuCode);
                    stock.setQuantity(stockQuantity);
                    stock.setAvailableQuantity(stockQuantity);
                    stock.setLockedQuantity(BigDecimal.ZERO);
                    stock.setTotalAmount(item.getAmount());
                    stock.setAvgCostPrice(item.getUnitPrice());
                    stock.setStatus(1);
                    stockMapper.insert(stock);
                } else {
                    stock.setGoodsCode(goodsCode);
                    stock.setQuantity(stockQuantity);
                    stock.setAvailableQuantity(stockQuantity);
                    stock.setTotalAmount(item.getAmount());
                    stock.setAvgCostPrice(item.getUnitPrice());
                    stockMapper.updateById(stock);
                }
            }
        }
        super.updateById(entity);
        syncSend(entity);
        return CommonResult.success();
    }
}
