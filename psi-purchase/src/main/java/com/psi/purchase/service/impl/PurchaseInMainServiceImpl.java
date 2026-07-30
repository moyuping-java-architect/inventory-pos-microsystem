package com.psi.purchase.service.impl;

import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.purchase.dto.PurchaseInItemDTO;
import com.psi.purchase.dto.PurchaseInItemSaveDTO;
import com.psi.purchase.dto.PurchaseInMainDTO;
import com.psi.purchase.dto.PurchaseInQueryDTO;
import com.psi.purchase.dto.PurchaseInSaveDTO;
import com.psi.purchase.entity.PurchaseInItemEntity;
import com.psi.purchase.entity.PurchaseInMainEntity;
import com.psi.purchase.mapper.PurchaseInMainMapper;
import com.psi.purchase.mq.producer.PurchaseSyncProducer;
import com.psi.purchase.service.PurchaseInItemService;
import com.psi.purchase.service.PurchaseInMainService;
import com.psi.common.result.CommonResult;
import org.springframework.jdbc.core.JdbcTemplate;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PurchaseInMainServiceImpl extends ServiceImpl<PurchaseInMainMapper, PurchaseInMainEntity> implements PurchaseInMainService {

    private final PurchaseInItemService purchaseInItemService;
    private final MqMessageFacade mqMessageFacade;
    private final JdbcTemplate jdbcTemplate;
    private final PurchaseSyncProducer purchaseSyncProducer;

    public PurchaseInMainServiceImpl(PurchaseInItemService purchaseInItemService, MqMessageFacade mqMessageFacade, JdbcTemplate jdbcTemplate, PurchaseSyncProducer purchaseSyncProducer) {
        this.purchaseInItemService = purchaseInItemService;
        this.mqMessageFacade = mqMessageFacade;
        this.jdbcTemplate = jdbcTemplate;
        this.purchaseSyncProducer = purchaseSyncProducer;
    }

    @Override
    public CommonResult<PurchaseInMainDTO> getById(Long id) {
        PurchaseInMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<PurchaseInMainDTO> list(PurchaseInQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new PurchaseInQueryDTO();
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        Page<PurchaseInMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<PurchaseInMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getInNo() != null) {
            wrapper.like(PurchaseInMainEntity::getInNo, queryDTO.getInNo());
        }
        if (queryDTO.getOrderNo() != null) {
            wrapper.like(PurchaseInMainEntity::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getSupplierCode() != null) {
            wrapper.like(PurchaseInMainEntity::getSupplierCode, queryDTO.getSupplierCode());
        }
        if (queryDTO.getSupplierName() != null) {
            wrapper.like(PurchaseInMainEntity::getSupplierName, queryDTO.getSupplierName());
        }
        if (queryDTO.getInDate() != null) {
            wrapper.eq(PurchaseInMainEntity::getInDate, queryDTO.getInDate());
        }
        if (queryDTO.getWarehouseCode() != null) {
            wrapper.eq(PurchaseInMainEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (queryDTO.getInStatus() != null) {
            wrapper.eq(PurchaseInMainEntity::getInStatus, queryDTO.getInStatus());
        }
        if (queryDTO.getAuditStatus() != null) {
            wrapper.eq(PurchaseInMainEntity::getAuditStatus, queryDTO.getAuditStatus());
        }
        
        IPage<PurchaseInMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<PurchaseInMainDTO> save(PurchaseInSaveDTO saveDTO) {
        PurchaseInMainEntity entity = BeanUtils.convert(saveDTO, PurchaseInMainEntity.class);
        if (entity.getInNo() == null || entity.getInNo().trim().isEmpty()) {
            entity.setInNo("PI" + IdUtils.generateId());
        }
        entity.setInStatus(1);
        entity.setAuditStatus(0);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        List<PurchaseInItemSaveDTO> itemList = saveDTO.getItems();
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        for (PurchaseInItemSaveDTO itemSaveDTO : itemList) {
            BigDecimal itemAmount = itemSaveDTO.getInQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        entity.setPayAmount(totalAmount.add(taxAmount));
        
        super.save(entity);
        
        List<PurchaseInItemEntity> items = new ArrayList<>();
        List<PurchaseInItemSaveDTO> updateItemList = saveDTO.getItems();
        if (updateItemList == null) {
            updateItemList = new ArrayList<>();
        }
        for (PurchaseInItemSaveDTO itemSaveDTO : updateItemList) {
            PurchaseInItemEntity item = BeanUtils.convert(itemSaveDTO, PurchaseInItemEntity.class);
            item.setInId(entity.getId());
            item.setInNo(entity.getInNo());
            BigDecimal itemAmount = itemSaveDTO.getInQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }

        if (!items.isEmpty()) {
            purchaseInItemService.saveBatch(items);
        }

        sendPurchaseInMain(entity);
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<PurchaseInMainDTO> update(Long id, PurchaseInSaveDTO saveDTO) {
        PurchaseInMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        List<PurchaseInItemSaveDTO> updateItemList2 = saveDTO.getItems();
        if (updateItemList2 == null) {
            updateItemList2 = new ArrayList<>();
        }
        for (PurchaseInItemSaveDTO itemSaveDTO : updateItemList2) {
            BigDecimal itemAmount = itemSaveDTO.getInQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }

        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        entity.setPayAmount(totalAmount.add(taxAmount));

        super.updateById(entity);

        purchaseInItemService.remove(new LambdaQueryWrapper<PurchaseInItemEntity>().eq(PurchaseInItemEntity::getInId, id));

        List<PurchaseInItemEntity> items = new ArrayList<>();
        for (PurchaseInItemSaveDTO itemSaveDTO : updateItemList2) {
            PurchaseInItemEntity item = BeanUtils.convert(itemSaveDTO, PurchaseInItemEntity.class);
            item.setInId(entity.getId());
            item.setInNo(entity.getInNo());
            BigDecimal itemAmount = itemSaveDTO.getInQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }

        if (!items.isEmpty()) {
            purchaseInItemService.saveBatch(items);
        }

        sendPurchaseInMain(entity);
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        purchaseInItemService.remove(new LambdaQueryWrapper<PurchaseInItemEntity>().eq(PurchaseInItemEntity::getInId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> audit(Long id, Integer auditStatus) {
        PurchaseInMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setAuditStatus(auditStatus);
        entity.setAuditTime(LocalDateTime.now());
        if (auditStatus == 1) {
            entity.setInStatus(2);
        }
        super.updateById(entity);
        sendPurchaseInMain(entity);

        // 审批通过后发送库存增加消息
        if (auditStatus == 1) {
            sendStockIncreaseMessage(entity);
        }

        return CommonResult.success();
    }
    
    /**
     * 获取 SKU+业务单位 对应的换算率
     * 优先使用业务明细中保存的换算率（单据快照），为空则兜底查询
     */
    private BigDecimal getConversionRate(String skuCode, String unitCode, BigDecimal itemRate) {
        if (itemRate != null && itemRate.compareTo(BigDecimal.ZERO) > 0) {
            return itemRate;
        }
        if (skuCode == null || unitCode == null || unitCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        try {
            BigDecimal rate = jdbcTemplate.queryForObject(
                    "SELECT conversion_rate FROM psi_goods.goods_sku_sale_unit WHERE sku_code = ? AND symbol = ? LIMIT 1",
                    BigDecimal.class, skuCode, unitCode);
            if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                return rate;
            }
        } catch (Exception e) {
            log.warn("查询SKU[{}]单位[{}]换算率失败: {}", skuCode, unitCode, e.getMessage());
        }
        log.warn("未找到SKU[{}]销售单位[{}]的换算率，按1:1处理", skuCode, unitCode);
        return BigDecimal.ONE;
    }

    private void sendStockIncreaseMessage(PurchaseInMainEntity entity) {
        try {
            List<PurchaseInItemEntity> items = purchaseInItemService.list(
                new LambdaQueryWrapper<PurchaseInItemEntity>().eq(PurchaseInItemEntity::getInId, entity.getId())
            );
            
            if (items == null || items.isEmpty()) {
                return;
            }
            
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (PurchaseInItemEntity item : items) {
                Map<String, Object> itemMap = new HashMap<>();
                BigDecimal conversionRate = getConversionRate(item.getSkuCode(), item.getUnitCode(), item.getConversionRate());
                BigDecimal stockQuantity = item.getInQuantity().multiply(conversionRate);
                itemMap.put("skuCode", item.getSkuCode());
                itemMap.put("goodsCode", item.getGoodsCode());
                itemMap.put("quantity", item.getInQuantity());
                itemMap.put("stockQuantity", stockQuantity);
                itemMap.put("unitCode", item.getUnitCode());
                itemMap.put("batchNo", item.getBatchNo());
                itemMap.put("costPrice", item.getUnitPrice());
                itemList.add(itemMap);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("inNo", entity.getInNo());
            data.put("warehouseCode", entity.getWarehouseCode());
            data.put("items", itemList);
            
            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.PURCHASE_IN_EXCHANGE,
                    RabbitMQConstant.PURCHASE_IN_STOCK_ROUTING_KEY,
                    "PURCHASE_IN_STOCK_INCREASE"
            );
            
            mqMessageFacade.sendAsync(message);
            
            log.info("采购入库库存增加消息已发送: inNo={}", entity.getInNo());
        } catch (Exception e) {
            log.error("发送采购入库库存增加消息失败: inNo={}, error={}", entity.getInNo(), e.getMessage(), e);
        }
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        PurchaseInMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        sendPurchaseInMain(entity);
        return CommonResult.success();
    }

    private void sendPurchaseInMain(PurchaseInMainEntity entity) {
        try {
            purchaseSyncProducer.sendPurchaseInMain(entity);
        } catch (Exception e) {
            log.error("采购入库主表实时同步消息发送失败", e);
        }
    }

    private PurchaseInMainDTO convertToDTO(PurchaseInMainEntity entity) {
        PurchaseInMainDTO dto = BeanUtils.convert(entity, PurchaseInMainDTO.class);

        List<PurchaseInItemEntity> items = purchaseInItemService.list(
            new LambdaQueryWrapper<PurchaseInItemEntity>().eq(PurchaseInItemEntity::getInId, entity.getId())
        );

        if (items == null) {
            items = new ArrayList<>();
        }

        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, PurchaseInItemDTO.class)).toList());
        return dto;
    }
}