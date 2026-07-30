package com.psi.sale.service.impl;

import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.sale.dto.SaleReturnItemDTO;
import com.psi.sale.dto.SaleReturnItemSaveDTO;
import com.psi.sale.dto.SaleReturnMainDTO;
import com.psi.sale.dto.SaleReturnQueryDTO;
import com.psi.sale.dto.SaleReturnSaveDTO;
import com.psi.sale.entity.SaleReturnItemEntity;
import com.psi.sale.entity.SaleReturnMainEntity;
import com.psi.sale.mapper.SaleReturnMainMapper;
import com.psi.sale.service.SaleReturnItemService;
import com.psi.sale.service.SaleReturnMainService;
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

@Slf4j
@Service
public class SaleReturnMainServiceImpl extends ServiceImpl<SaleReturnMainMapper, SaleReturnMainEntity> implements SaleReturnMainService {

    private final SaleReturnItemService saleReturnItemService;
    private final MqMessageFacade mqMessageFacade;
    private final JdbcTemplate jdbcTemplate;

    public SaleReturnMainServiceImpl(SaleReturnItemService saleReturnItemService, MqMessageFacade mqMessageFacade, JdbcTemplate jdbcTemplate) {
        this.saleReturnItemService = saleReturnItemService;
        this.mqMessageFacade = mqMessageFacade;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CommonResult<SaleReturnMainDTO> getById(Long id) {
        SaleReturnMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<SaleReturnMainDTO> list(SaleReturnQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new SaleReturnQueryDTO();
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        Page<SaleReturnMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SaleReturnMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getReturnNo() != null) {
            wrapper.like(SaleReturnMainEntity::getReturnNo, queryDTO.getReturnNo());
        }
        if (queryDTO.getOutNo() != null) {
            wrapper.like(SaleReturnMainEntity::getOutNo, queryDTO.getOutNo());
        }
        if (queryDTO.getOrderNo() != null) {
            wrapper.like(SaleReturnMainEntity::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getCustomerCode() != null) {
            wrapper.like(SaleReturnMainEntity::getCustomerCode, queryDTO.getCustomerCode());
        }
        if (queryDTO.getCustomerName() != null) {
            wrapper.like(SaleReturnMainEntity::getCustomerName, queryDTO.getCustomerName());
        }
        if (queryDTO.getReturnDate() != null) {
            wrapper.eq(SaleReturnMainEntity::getReturnDate, queryDTO.getReturnDate());
        }
        
        IPage<SaleReturnMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<SaleReturnMainDTO> save(SaleReturnSaveDTO saveDTO) {
        SaleReturnMainEntity entity = BeanUtils.convert(saveDTO, SaleReturnMainEntity.class);
        if (entity.getReturnNo() == null || entity.getReturnNo().trim().isEmpty()) {
            entity.setReturnNo("SRT" + IdUtils.generateId());
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        List<SaleReturnItemSaveDTO> itemList = saveDTO.getItems();
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        for (SaleReturnItemSaveDTO itemSaveDTO : itemList) {
            BigDecimal itemAmount = itemSaveDTO.getReturnQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }
        
        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        entity.setPayAmount(totalAmount.add(taxAmount));
        
        super.save(entity);
        
        List<SaleReturnItemEntity> items = new ArrayList<>();
        List<SaleReturnItemSaveDTO> updateItemList = saveDTO.getItems();
        if (updateItemList == null) {
            updateItemList = new ArrayList<>();
        }
        for (SaleReturnItemSaveDTO itemSaveDTO : updateItemList) {
            SaleReturnItemEntity item = BeanUtils.convert(itemSaveDTO, SaleReturnItemEntity.class);
            item.setReturnId(entity.getId());
            item.setReturnNo(entity.getReturnNo());
            BigDecimal itemAmount = itemSaveDTO.getReturnQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }

        if (!items.isEmpty()) {
            saleReturnItemService.saveBatch(items);
        }

        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<SaleReturnMainDTO> update(Long id, SaleReturnSaveDTO saveDTO) {
        SaleReturnMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        List<SaleReturnItemSaveDTO> updateItemList2 = saveDTO.getItems();
        if (updateItemList2 == null) {
            updateItemList2 = new ArrayList<>();
        }
        for (SaleReturnItemSaveDTO itemSaveDTO : updateItemList2) {
            BigDecimal itemAmount = itemSaveDTO.getReturnQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            totalAmount = totalAmount.add(itemAmount);
            taxAmount = taxAmount.add(itemTax);
        }

        entity.setTotalAmount(totalAmount);
        entity.setTaxAmount(taxAmount);
        entity.setPayAmount(totalAmount.add(taxAmount));

        super.updateById(entity);

        saleReturnItemService.remove(new LambdaQueryWrapper<SaleReturnItemEntity>().eq(SaleReturnItemEntity::getReturnId, id));

        List<SaleReturnItemEntity> items = new ArrayList<>();
        for (SaleReturnItemSaveDTO itemSaveDTO : updateItemList2) {
            SaleReturnItemEntity item = BeanUtils.convert(itemSaveDTO, SaleReturnItemEntity.class);
            item.setReturnId(entity.getId());
            item.setReturnNo(entity.getReturnNo());
            BigDecimal itemAmount = itemSaveDTO.getReturnQuantity().multiply(itemSaveDTO.getUnitPrice());
            BigDecimal itemTax = itemAmount.multiply(itemSaveDTO.getTaxRate());

            item.setAmount(itemAmount);
            item.setTaxAmount(itemTax);
            items.add(item);
        }

        if (!items.isEmpty()) {
            saleReturnItemService.saveBatch(items);
        }

        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        saleReturnItemService.remove(new LambdaQueryWrapper<SaleReturnItemEntity>().eq(SaleReturnItemEntity::getReturnId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SaleReturnMainEntity entity = super.getById(id);
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
        SaleReturnMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        if (auditStatus == 1) {
            entity.setOrderStatus(2);
        }
        super.updateById(entity);
        
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

    private void sendStockIncreaseMessage(SaleReturnMainEntity entity) {
        try {
            List<SaleReturnItemEntity> items = saleReturnItemService.list(
                new LambdaQueryWrapper<SaleReturnItemEntity>().eq(SaleReturnItemEntity::getReturnId, entity.getId())
            );
            
            if (items == null || items.isEmpty()) {
                return;
            }
            
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (SaleReturnItemEntity item : items) {
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
                    RabbitMQConstant.SALE_RETURN_EXCHANGE,
                    RabbitMQConstant.SALE_RETURN_STOCK_ROUTING_KEY,
                    "SALE_RETURN_STOCK_INCREASE"
            );
            
            mqMessageFacade.sendAsync(message);
            
            log.info("销售退货库存增加消息已发送: returnNo={}", entity.getReturnNo());
        } catch (Exception e) {
            log.error("发送销售退货库存增加消息失败: returnNo={}, error={}", entity.getReturnNo(), e.getMessage(), e);
        }
    }

    private SaleReturnMainDTO convertToDTO(SaleReturnMainEntity entity) {
        SaleReturnMainDTO dto = BeanUtils.convert(entity, SaleReturnMainDTO.class);

        List<SaleReturnItemEntity> items = saleReturnItemService.list(
            new LambdaQueryWrapper<SaleReturnItemEntity>().eq(SaleReturnItemEntity::getReturnId, entity.getId())
        );

        if (items == null) {
            items = new ArrayList<>();
        }

        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, SaleReturnItemDTO.class)).toList());
        return dto;
    }
}