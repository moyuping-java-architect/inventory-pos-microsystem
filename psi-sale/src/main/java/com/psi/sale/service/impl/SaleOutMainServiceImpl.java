package com.psi.sale.service.impl;

import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.sale.dto.SaleOutItemDTO;
import com.psi.sale.dto.SaleOutItemSaveDTO;
import com.psi.sale.dto.SaleOutMainDTO;
import com.psi.sale.dto.SaleOutQueryDTO;
import com.psi.sale.dto.SaleOutSaveDTO;
import com.psi.sale.entity.SaleOutItemEntity;
import com.psi.sale.entity.SaleOutMainEntity;
import com.psi.sale.mapper.SaleOutMainMapper;
import com.psi.sale.service.SaleOutItemService;
import com.psi.sale.service.SaleOutMainService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;

@Slf4j
@Service
public class SaleOutMainServiceImpl extends ServiceImpl<SaleOutMainMapper, SaleOutMainEntity> implements SaleOutMainService {

    private final SaleOutItemService saleOutItemService;
    private final MqMessageFacade mqMessageFacade;
    private final JdbcTemplate jdbcTemplate;

    public SaleOutMainServiceImpl(SaleOutItemService saleOutItemService, MqMessageFacade mqMessageFacade, JdbcTemplate jdbcTemplate) {
        this.saleOutItemService = saleOutItemService;
        this.mqMessageFacade = mqMessageFacade;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CommonResult<SaleOutMainDTO> getById(Long id) {
        SaleOutMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<SaleOutMainDTO> list(SaleOutQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new SaleOutQueryDTO();
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        Page<SaleOutMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SaleOutMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getOutNo() != null) {
            wrapper.like(SaleOutMainEntity::getOutNo, queryDTO.getOutNo());
        }
        if (queryDTO.getOrderNo() != null) {
            wrapper.like(SaleOutMainEntity::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getCustomerCode() != null) {
            wrapper.like(SaleOutMainEntity::getCustomerCode, queryDTO.getCustomerCode());
        }
        if (queryDTO.getCustomerName() != null) {
            wrapper.like(SaleOutMainEntity::getCustomerName, queryDTO.getCustomerName());
        }
        if (queryDTO.getOutDate() != null) {
            wrapper.eq(SaleOutMainEntity::getOutDate, queryDTO.getOutDate());
        }
        if (queryDTO.getWarehouseCode() != null) {
            wrapper.eq(SaleOutMainEntity::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        
        IPage<SaleOutMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<SaleOutMainDTO> save(SaleOutSaveDTO saveDTO) {
        SaleOutMainEntity entity = BeanUtils.convert(saveDTO, SaleOutMainEntity.class);
        if (entity.getOutNo() == null || entity.getOutNo().trim().isEmpty()) {
            entity.setOutNo("SOUT" + IdUtils.generateId());
        }
        entity.setOrderStatus(1);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        List<SaleOutItemSaveDTO> itemList = saveDTO.getItems();
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        for (SaleOutItemSaveDTO itemSaveDTO : itemList) {
            BigDecimal itemAmount = itemSaveDTO.getOutQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        
        super.save(entity);
        
        List<SaleOutItemEntity> items = new ArrayList<>();
        List<SaleOutItemSaveDTO> updateItemList = saveDTO.getItems();
        if (updateItemList == null) {
            updateItemList = new ArrayList<>();
        }
        for (SaleOutItemSaveDTO itemSaveDTO : updateItemList) {
            SaleOutItemEntity item = BeanUtils.convert(itemSaveDTO, SaleOutItemEntity.class);
            item.setOutId(entity.getId());
            item.setOutNo(entity.getOutNo());
            BigDecimal itemAmount = itemSaveDTO.getOutQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }

        if (!items.isEmpty()) {
            saleOutItemService.saveBatch(items);
        }

        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<SaleOutMainDTO> update(Long id, SaleOutSaveDTO saveDTO) {
        SaleOutMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        List<SaleOutItemSaveDTO> updateItemList2 = saveDTO.getItems();
        if (updateItemList2 == null) {
            updateItemList2 = new ArrayList<>();
        }
        for (SaleOutItemSaveDTO itemSaveDTO : updateItemList2) {
            BigDecimal itemAmount = itemSaveDTO.getOutQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }

        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);

        super.updateById(entity);

        saleOutItemService.remove(new LambdaQueryWrapper<SaleOutItemEntity>().eq(SaleOutItemEntity::getOutId, id));

        List<SaleOutItemEntity> items = new ArrayList<>();
        for (SaleOutItemSaveDTO itemSaveDTO : updateItemList2) {
            SaleOutItemEntity item = BeanUtils.convert(itemSaveDTO, SaleOutItemEntity.class);
            item.setOutId(entity.getId());
            item.setOutNo(entity.getOutNo());
            BigDecimal itemAmount = itemSaveDTO.getOutQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }

        if (!items.isEmpty()) {
            saleOutItemService.saveBatch(items);
        }

        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        saleOutItemService.remove(new LambdaQueryWrapper<SaleOutItemEntity>().eq(SaleOutItemEntity::getOutId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SaleOutMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setStatus(status);
        super.updateById(entity);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<Void> audit(Long id, Integer auditStatus) {
        SaleOutMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        entity.setOrderStatus(auditStatus);
        super.updateById(entity);
        
        // 审批通过后发送库存扣减消息
        if (auditStatus == 2) {
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

    private void sendStockDecreaseMessage(SaleOutMainEntity entity) {
        try {
            List<SaleOutItemEntity> items = saleOutItemService.list(
                new LambdaQueryWrapper<SaleOutItemEntity>().eq(SaleOutItemEntity::getOutId, entity.getId())
            );
            
            if (items == null || items.isEmpty()) {
                return;
            }
            
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (SaleOutItemEntity item : items) {
                Map<String, Object> itemMap = new HashMap<>();
                BigDecimal conversionRate = getConversionRate(item.getSkuCode(), item.getUnitCode(), item.getConversionRate());
                BigDecimal stockQuantity = item.getOutQuantity().multiply(conversionRate);
                itemMap.put("skuCode", item.getSkuCode());
                itemMap.put("goodsCode", item.getGoodsCode());
                itemMap.put("quantity", item.getOutQuantity());
                itemMap.put("stockQuantity", stockQuantity);
                itemMap.put("unitCode", item.getUnitCode());
                itemMap.put("batchNo", item.getBatchNo());
                itemList.add(itemMap);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("outNo", entity.getOutNo());
            data.put("orderNo", entity.getOrderNo());
            data.put("warehouseCode", entity.getWarehouseCode());
            data.put("action", "CONFIRM");
            data.put("items", itemList);
            
            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.SALE_OUT_EXCHANGE,
                    RabbitMQConstant.SALE_OUT_STOCK_ROUTING_KEY,
                    "SALE_OUT_STOCK_CONFIRM"
            );
            
            mqMessageFacade.sendAsync(message);
            
            log.info("销售出库库存确认消息已发送: outNo={}", entity.getOutNo());
        } catch (Exception e) {
            log.error("发送销售出库库存确认消息失败: outNo={}, error={}", entity.getOutNo(), e.getMessage(), e);
        }
    }

    private SaleOutMainDTO convertToDTO(SaleOutMainEntity entity) {
        SaleOutMainDTO dto = BeanUtils.convert(entity, SaleOutMainDTO.class);

        List<SaleOutItemEntity> items = saleOutItemService.list(
            new LambdaQueryWrapper<SaleOutItemEntity>().eq(SaleOutItemEntity::getOutId, entity.getId())
        );

        if (items == null) {
            items = new ArrayList<>();
        }

        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, SaleOutItemDTO.class)).toList());
        return dto;
    }
}