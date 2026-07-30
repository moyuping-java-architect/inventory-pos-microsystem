package com.psi.purchase.service.impl;

import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.purchase.dto.PurchaseReturnItemDTO;
import com.psi.purchase.dto.PurchaseReturnItemSaveDTO;
import com.psi.purchase.dto.PurchaseReturnMainDTO;
import com.psi.purchase.dto.PurchaseReturnQueryDTO;
import com.psi.purchase.dto.PurchaseReturnSaveDTO;
import com.psi.purchase.entity.PurchaseReturnItemEntity;
import com.psi.purchase.entity.PurchaseReturnMainEntity;
import com.psi.purchase.mapper.PurchaseReturnMainMapper;
import com.psi.purchase.mq.producer.PurchaseSyncProducer;
import com.psi.purchase.service.PurchaseReturnItemService;
import com.psi.purchase.service.PurchaseReturnMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.psi.common.util.IdUtils;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class PurchaseReturnMainServiceImpl extends ServiceImpl<PurchaseReturnMainMapper, PurchaseReturnMainEntity> implements PurchaseReturnMainService {

    private final PurchaseReturnItemService purchaseReturnItemService;
    private final MqMessageFacade mqMessageFacade;
    private final JdbcTemplate jdbcTemplate;
    private final PurchaseSyncProducer purchaseSyncProducer;

    public PurchaseReturnMainServiceImpl(PurchaseReturnItemService purchaseReturnItemService, MqMessageFacade mqMessageFacade, JdbcTemplate jdbcTemplate, PurchaseSyncProducer purchaseSyncProducer) {
        this.purchaseReturnItemService = purchaseReturnItemService;
        this.mqMessageFacade = mqMessageFacade;
        this.jdbcTemplate = jdbcTemplate;
        this.purchaseSyncProducer = purchaseSyncProducer;
    }

    @Override
    public CommonResult<PurchaseReturnMainDTO> getById(Long id) {
        PurchaseReturnMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<PurchaseReturnMainDTO> list(PurchaseReturnQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new PurchaseReturnQueryDTO();
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        Page<PurchaseReturnMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<PurchaseReturnMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getReturnNo() != null) {
            wrapper.like(PurchaseReturnMainEntity::getReturnNo, queryDTO.getReturnNo());
        }
        if (queryDTO.getInNo() != null) {
            wrapper.like(PurchaseReturnMainEntity::getInNo, queryDTO.getInNo());
        }
        if (queryDTO.getSupplierCode() != null) {
            wrapper.like(PurchaseReturnMainEntity::getSupplierCode, queryDTO.getSupplierCode());
        }
        if (queryDTO.getSupplierName() != null) {
            wrapper.like(PurchaseReturnMainEntity::getSupplierName, queryDTO.getSupplierName());
        }
        if (queryDTO.getReturnDate() != null) {
            wrapper.eq(PurchaseReturnMainEntity::getReturnDate, queryDTO.getReturnDate());
        }
        if (queryDTO.getReturnStatus() != null) {
            wrapper.eq(PurchaseReturnMainEntity::getReturnStatus, queryDTO.getReturnStatus());
        }
        if (queryDTO.getAuditStatus() != null) {
            wrapper.eq(PurchaseReturnMainEntity::getAuditStatus, queryDTO.getAuditStatus());
        }
        
        IPage<PurchaseReturnMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<PurchaseReturnMainDTO> save(PurchaseReturnSaveDTO saveDTO) {
        PurchaseReturnMainEntity entity = BeanUtils.convert(saveDTO, PurchaseReturnMainEntity.class);
        if (entity.getReturnNo() == null || entity.getReturnNo().trim().isEmpty()) {
            entity.setReturnNo("PR" + IdUtils.generateId());
        }
        entity.setReturnStatus(1);
        entity.setAuditStatus(0);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        List<PurchaseReturnItemSaveDTO> itemList = saveDTO.getItems();
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        for (PurchaseReturnItemSaveDTO itemSaveDTO : itemList) {
            BigDecimal itemAmount = itemSaveDTO.getReturnQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        entity.setPayAmount(totalAmount.add(taxAmount));
        
        super.save(entity);
        
        List<PurchaseReturnItemEntity> items = new ArrayList<>();
        List<PurchaseReturnItemSaveDTO> updateItemList = saveDTO.getItems();
        if (updateItemList == null) {
            updateItemList = new ArrayList<>();
        }
        for (PurchaseReturnItemSaveDTO itemSaveDTO : updateItemList) {
            PurchaseReturnItemEntity item = BeanUtils.convert(itemSaveDTO, PurchaseReturnItemEntity.class);
            item.setReturnId(entity.getId());
            item.setReturnNo(entity.getReturnNo());
            BigDecimal itemAmount = itemSaveDTO.getReturnQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }

        if (!items.isEmpty()) {
            purchaseReturnItemService.saveBatch(items);
        }

        sendPurchaseReturnMain(entity);
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<PurchaseReturnMainDTO> update(Long id, PurchaseReturnSaveDTO saveDTO) {
        PurchaseReturnMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        List<PurchaseReturnItemSaveDTO> updateItemList2 = saveDTO.getItems();
        if (updateItemList2 == null) {
            updateItemList2 = new ArrayList<>();
        }
        for (PurchaseReturnItemSaveDTO itemSaveDTO : updateItemList2) {
            BigDecimal itemAmount = itemSaveDTO.getReturnQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }

        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        entity.setPayAmount(totalAmount.add(taxAmount));

        super.updateById(entity);

        purchaseReturnItemService.remove(new LambdaQueryWrapper<PurchaseReturnItemEntity>().eq(PurchaseReturnItemEntity::getReturnId, id));

        List<PurchaseReturnItemEntity> items = new ArrayList<>();
        for (PurchaseReturnItemSaveDTO itemSaveDTO : updateItemList2) {
            PurchaseReturnItemEntity item = BeanUtils.convert(itemSaveDTO, PurchaseReturnItemEntity.class);
            item.setReturnId(entity.getId());
            item.setReturnNo(entity.getReturnNo());
            BigDecimal itemAmount = itemSaveDTO.getReturnQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }

        if (!items.isEmpty()) {
            purchaseReturnItemService.saveBatch(items);
        }

        sendPurchaseReturnMain(entity);
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        purchaseReturnItemService.remove(new LambdaQueryWrapper<PurchaseReturnItemEntity>().eq(PurchaseReturnItemEntity::getReturnId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> audit(Long id, Integer auditStatus) {
        PurchaseReturnMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setAuditStatus(auditStatus);
        entity.setAuditTime(LocalDateTime.now());
        if (auditStatus == 1) {
            entity.setReturnStatus(2);
        }
        super.updateById(entity);
        sendPurchaseReturnMain(entity);

        // 审批通过后发送库存扣减消息
        if (auditStatus == 1) {
            sendStockDecreaseMessage(entity);
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

    private void sendStockDecreaseMessage(PurchaseReturnMainEntity entity) {
        try {
            List<PurchaseReturnItemEntity> items = purchaseReturnItemService.list(
                new LambdaQueryWrapper<PurchaseReturnItemEntity>().eq(PurchaseReturnItemEntity::getReturnId, entity.getId())
            );
            
            if (items == null || items.isEmpty()) {
                return;
            }
            
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (PurchaseReturnItemEntity item : items) {
                Map<String, Object> itemMap = new HashMap<>();
                BigDecimal conversionRate = getConversionRate(item.getSkuCode(), item.getUnitCode(), item.getConversionRate());
                BigDecimal stockQuantity = item.getReturnQuantity().multiply(conversionRate);
                itemMap.put("skuCode", item.getSkuCode());
                itemMap.put("goodsCode", item.getGoodsCode());
                itemMap.put("quantity", item.getReturnQuantity());
                itemMap.put("stockQuantity", stockQuantity);
                itemMap.put("unitCode", item.getUnitCode());
                itemMap.put("batchNo", item.getBatchNo());
                itemList.add(itemMap);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("returnNo", entity.getReturnNo());
            data.put("warehouseCode", entity.getWarehouseCode());
            data.put("items", itemList);
            
            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.PURCHASE_RETURN_EXCHANGE,
                    RabbitMQConstant.PURCHASE_RETURN_STOCK_ROUTING_KEY,
                    "PURCHASE_RETURN_STOCK_DECREASE"
            );
            
            mqMessageFacade.sendAsync(message);
            
            log.info("采购退货库存扣减消息已发送: returnNo={}", entity.getReturnNo());
        } catch (Exception e) {
            log.error("发送采购退货库存扣减消息失败: returnNo={}, error={}", entity.getReturnNo(), e.getMessage(), e);
        }
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        PurchaseReturnMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        sendPurchaseReturnMain(entity);
        return CommonResult.success();
    }

    private void sendPurchaseReturnMain(PurchaseReturnMainEntity entity) {
        try {
            purchaseSyncProducer.sendPurchaseReturnMain(entity);
        } catch (Exception e) {
            log.error("采购退货主表实时同步消息发送失败", e);
        }
    }

    private PurchaseReturnMainDTO convertToDTO(PurchaseReturnMainEntity entity) {
        PurchaseReturnMainDTO dto = BeanUtils.convert(entity, PurchaseReturnMainDTO.class);

        List<PurchaseReturnItemEntity> items = purchaseReturnItemService.list(
            new LambdaQueryWrapper<PurchaseReturnItemEntity>().eq(PurchaseReturnItemEntity::getReturnId, entity.getId())
        );

        if (items == null) {
            items = new ArrayList<>();
        }

        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, PurchaseReturnItemDTO.class)).toList());
        return dto;
    }
}