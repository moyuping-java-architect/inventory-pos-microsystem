package com.psi.sale.service.impl;

import com.psi.sale.dto.SaleOrderItemDTO;
import com.psi.sale.dto.SaleOrderItemSaveDTO;
import com.psi.sale.dto.SaleOrderMainDTO;
import com.psi.sale.dto.SaleOrderQueryDTO;
import com.psi.sale.dto.SaleOrderSaveDTO;
import com.psi.sale.entity.SaleOrderItemEntity;
import com.psi.sale.entity.SaleOrderMainEntity;
import com.psi.sale.mapper.SaleOrderMainMapper;
import com.psi.sale.service.SaleOrderItemService;
import com.psi.sale.service.SaleOrderMainService;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.message.MessageFactory;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.psi.common.util.IdUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SaleOrderMainServiceImpl extends ServiceImpl<SaleOrderMainMapper, SaleOrderMainEntity> implements SaleOrderMainService {

    private final SaleOrderItemService saleOrderItemService;
    private final MqMessageFacade mqMessageFacade;
    private final JdbcTemplate jdbcTemplate;

    public SaleOrderMainServiceImpl(SaleOrderItemService saleOrderItemService, MqMessageFacade mqMessageFacade, JdbcTemplate jdbcTemplate) {
        this.saleOrderItemService = saleOrderItemService;
        this.mqMessageFacade = mqMessageFacade;
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.16");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Override
    public CommonResult<SaleOrderMainDTO> getById(Long id) {
        SaleOrderMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    public PageResult<SaleOrderMainDTO> list(SaleOrderQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new SaleOrderQueryDTO();
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        Page<SaleOrderMainEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SaleOrderMainEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getOrderNo() != null) {
            wrapper.like(SaleOrderMainEntity::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getCustomerCode() != null) {
            wrapper.like(SaleOrderMainEntity::getCustomerCode, queryDTO.getCustomerCode());
        }
        if (queryDTO.getCustomerName() != null) {
            wrapper.like(SaleOrderMainEntity::getCustomerName, queryDTO.getCustomerName());
        }
        if (queryDTO.getOrderDate() != null) {
            wrapper.eq(SaleOrderMainEntity::getOrderDate, queryDTO.getOrderDate());
        }
        if (queryDTO.getOrderStatus() != null) {
            wrapper.eq(SaleOrderMainEntity::getOrderStatus, queryDTO.getOrderStatus());
        }
        
        IPage<SaleOrderMainEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<SaleOrderMainDTO> save(SaleOrderSaveDTO saveDTO) {
        SaleOrderMainEntity entity = BeanUtils.convert(saveDTO, SaleOrderMainEntity.class);
        if (entity.getOrderNo() == null || entity.getOrderNo().trim().isEmpty()) {
            entity.setOrderNo("SO" + IdUtils.generateId());
        }
        entity.setOrderStatus(1);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        List<SaleOrderItemSaveDTO> itemList = saveDTO.getItems();
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        for (SaleOrderItemSaveDTO itemSaveDTO : itemList) {
            TaxInfo taxInfo = computeItemTax(itemSaveDTO);
            BigDecimal itemDiscount = taxInfo.netAmount.multiply(
                    itemSaveDTO.getDiscountRate() != null ? itemSaveDTO.getDiscountRate() : BigDecimal.ZERO);

            totalAmount = totalAmount.add(taxInfo.netAmount);
            taxAmount = taxAmount.add(taxInfo.taxAmount);
            discountAmount = discountAmount.add(itemDiscount);
        }

        entity.setTotalAmount(saveDTO.getTotalAmount() != null ? saveDTO.getTotalAmount() : totalAmount);
        entity.setTaxAmount(saveDTO.getTaxAmount() != null ? saveDTO.getTaxAmount() : taxAmount);
        entity.setDiscountAmount(saveDTO.getDiscountAmount() != null ? saveDTO.getDiscountAmount() : discountAmount);
        // 如果 payAmount 为 null 或 0，则使用计算出的默认值（总金额 + 税额 - 折扣）
        if (saveDTO.getPayAmount() != null && saveDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0) {
            entity.setPayAmount(saveDTO.getPayAmount());
        } else {
            entity.setPayAmount(totalAmount.add(taxAmount).subtract(discountAmount));
        }

        super.save(entity);

        List<SaleOrderItemEntity> items = new ArrayList<>();
        List<SaleOrderItemSaveDTO> updateItemList = saveDTO.getItems();
        if (updateItemList == null) {
            updateItemList = new ArrayList<>();
        }
        for (SaleOrderItemSaveDTO itemSaveDTO : updateItemList) {
            SaleOrderItemEntity item = BeanUtils.convert(itemSaveDTO, SaleOrderItemEntity.class);
            item.setOrderId(entity.getId());
            item.setOrderNo(entity.getOrderNo());
            TaxInfo taxInfo = computeItemTax(itemSaveDTO);
            BigDecimal itemDiscount = taxInfo.netAmount.multiply(
                    itemSaveDTO.getDiscountRate() != null ? itemSaveDTO.getDiscountRate() : BigDecimal.ZERO);

            item.setAmount(taxInfo.netAmount);
            item.setTaxRate(taxInfo.taxRate);
            item.setIsTaxInclusive(taxInfo.isTaxInclusive);
            item.setTaxAmount(taxInfo.taxAmount);
            item.setDiscountAmount(itemDiscount);
            item.setNetAmount(taxInfo.netAmount.add(taxInfo.taxAmount).subtract(itemDiscount));
            items.add(item);
        }

        if (!items.isEmpty()) {
            saleOrderItemService.saveBatch(items);
        }

        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<SaleOrderMainDTO> update(Long id, SaleOrderSaveDTO saveDTO) {
        SaleOrderMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        
        BeanUtils.copyProperties(saveDTO, entity);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        for (SaleOrderItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            TaxInfo taxInfo = computeItemTax(itemSaveDTO);
            BigDecimal itemDiscount = taxInfo.netAmount.multiply(
                    itemSaveDTO.getDiscountRate() != null ? itemSaveDTO.getDiscountRate() : BigDecimal.ZERO);

            totalAmount = totalAmount.add(taxInfo.netAmount);
            taxAmount = taxAmount.add(taxInfo.taxAmount);
            discountAmount = discountAmount.add(itemDiscount);
        }

        entity.setTotalAmount(saveDTO.getTotalAmount() != null ? saveDTO.getTotalAmount() : totalAmount);
        entity.setTaxAmount(saveDTO.getTaxAmount() != null ? saveDTO.getTaxAmount() : taxAmount);
        entity.setDiscountAmount(saveDTO.getDiscountAmount() != null ? saveDTO.getDiscountAmount() : discountAmount);
        if (saveDTO.getPayAmount() != null) {
            entity.setPayAmount(saveDTO.getPayAmount());
        } else {
            entity.setPayAmount(totalAmount.add(taxAmount).subtract(discountAmount));
        }

        super.updateById(entity);

        saleOrderItemService.remove(new LambdaQueryWrapper<SaleOrderItemEntity>().eq(SaleOrderItemEntity::getOrderId, id));

        List<SaleOrderItemEntity> items = new ArrayList<>();
        for (SaleOrderItemSaveDTO itemSaveDTO : saveDTO.getItems()) {
            SaleOrderItemEntity item = BeanUtils.convert(itemSaveDTO, SaleOrderItemEntity.class);
            item.setOrderId(entity.getId());
            item.setOrderNo(entity.getOrderNo());
            TaxInfo taxInfo = computeItemTax(itemSaveDTO);
            BigDecimal itemDiscount = taxInfo.netAmount.multiply(
                    itemSaveDTO.getDiscountRate() != null ? itemSaveDTO.getDiscountRate() : BigDecimal.ZERO);

            item.setAmount(taxInfo.netAmount);
            item.setTaxRate(taxInfo.taxRate);
            item.setIsTaxInclusive(taxInfo.isTaxInclusive);
            item.setTaxAmount(taxInfo.taxAmount);
            item.setDiscountAmount(itemDiscount);
            item.setNetAmount(taxInfo.netAmount.add(taxInfo.taxAmount).subtract(itemDiscount));
            items.add(item);
        }

        if (!items.isEmpty()) {
            saleOrderItemService.saveBatch(items);
        }

        return CommonResult.success(convertToDTO(entity));
    }

    @Override
    @Transactional
    public CommonResult<Void> delete(Long id) {
        saleOrderItemService.remove(new LambdaQueryWrapper<SaleOrderItemEntity>().eq(SaleOrderItemEntity::getOrderId, id));
        if (!super.removeById(id)) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SaleOrderMainEntity entity = super.getById(id);
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
        SaleOrderMainEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        if (auditStatus == 1) {
            entity.setOrderStatus(2);
        }
        super.updateById(entity);

        // 审批通过后发送库存预占消息
        if (auditStatus == 1) {
            sendStockLockMessage(entity);
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

    private void sendStockLockMessage(SaleOrderMainEntity entity) {
        try {
            List<SaleOrderItemEntity> items = saleOrderItemService.list(
                new LambdaQueryWrapper<SaleOrderItemEntity>().eq(SaleOrderItemEntity::getOrderId, entity.getId())
            );

            if (items == null || items.isEmpty()) {
                return;
            }

            List<Map<String, Object>> itemList = new ArrayList<>();
            for (SaleOrderItemEntity item : items) {
                Map<String, Object> itemMap = new HashMap<>();
                BigDecimal conversionRate = getConversionRate(item.getSkuCode(), item.getUnitCode(), item.getConversionRate());
                BigDecimal stockQuantity = item.getQuantity().multiply(conversionRate);
                itemMap.put("skuCode", item.getSkuCode());
                itemMap.put("goodsCode", item.getGoodsCode());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("stockQuantity", stockQuantity);
                itemMap.put("unitCode", item.getUnitCode());
                itemList.add(itemMap);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", entity.getOrderNo());
            data.put("warehouseCode", entity.getWarehouseCode());
            data.put("action", "LOCK");
            data.put("items", itemList);

            MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                    data,
                    RabbitMQConstant.SALE_ORDER_EXCHANGE,
                    RabbitMQConstant.SALE_ORDER_STOCK_ROUTING_KEY,
                    "SALE_ORDER_STOCK_LOCK"
            );

            mqMessageFacade.sendAsync(message);

            log.info("销售订单库存预占消息已发送: orderNo={}", entity.getOrderNo());
        } catch (Exception e) {
            log.error("发送销售订单库存预占消息失败: orderNo={}, error={}", entity.getOrderNo(), e.getMessage(), e);
        }
    }

    private SaleOrderMainDTO convertToDTO(SaleOrderMainEntity entity) {
        SaleOrderMainDTO dto = BeanUtils.convert(entity, SaleOrderMainDTO.class);

        List<SaleOrderItemEntity> items = saleOrderItemService.list(
            new LambdaQueryWrapper<SaleOrderItemEntity>().eq(SaleOrderItemEntity::getOrderId, entity.getId())
        );

        if (items == null) {
            items = new ArrayList<>();
        }

        dto.setItems(items.stream().map(item -> BeanUtils.convert(item, SaleOrderItemDTO.class)).toList());
        return dto;
    }

    private TaxInfo computeItemTax(SaleOrderItemSaveDTO item) {
        BigDecimal taxRate = item.getTaxRate() != null ? item.getTaxRate() : DEFAULT_TAX_RATE;
        int isTaxInclusive = item.getIsTaxInclusive() != null && item.getIsTaxInclusive() == 1 ? 1 : 0;
        BigDecimal grossAmount = item.getQuantity().multiply(item.getUnitPrice());
        BigDecimal netAmount;
        BigDecimal taxAmount;
        if (isTaxInclusive == 1) {
            netAmount = grossAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.HALF_UP);
            taxAmount = netAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
            netAmount = grossAmount.subtract(taxAmount);
        } else {
            netAmount = grossAmount;
            taxAmount = grossAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        }
        return new TaxInfo(taxRate, isTaxInclusive, netAmount.setScale(2, RoundingMode.HALF_UP), taxAmount);
    }

    private record TaxInfo(BigDecimal taxRate, int isTaxInclusive, BigDecimal netAmount, BigDecimal taxAmount) {
    }
}