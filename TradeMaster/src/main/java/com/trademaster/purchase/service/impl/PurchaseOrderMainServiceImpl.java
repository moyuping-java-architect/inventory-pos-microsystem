package com.trademaster.purchase.service.impl;

import com.trademaster.purchase.entity.PurchaseOrderItem;
import com.trademaster.purchase.entity.PurchaseOrderMain;
import com.trademaster.purchase.mapper.PurchaseOrderItemMapper;
import com.trademaster.purchase.mapper.PurchaseOrderMainMapper;
import com.trademaster.purchase.service.PurchaseOrderMainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PurchaseOrderMainServiceImpl implements PurchaseOrderMainService {

    private final PurchaseOrderMainMapper orderMainMapper;
    private final PurchaseOrderItemMapper orderItemMapper;

    public PurchaseOrderMainServiceImpl(PurchaseOrderMainMapper orderMainMapper, PurchaseOrderItemMapper orderItemMapper) {
        this.orderMainMapper = orderMainMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional
    public PurchaseOrderMain save(String orderNo, String docName, String supplierCode, String supplierName,
                                  Integer paymentType, String currencyCode, BigDecimal exchangeRate,
                                  BigDecimal totalAmount, BigDecimal taxAmount, BigDecimal discountAmount,
                                  BigDecimal payAmount, LocalDateTime orderDate, LocalDateTime deliveryDate,
                                  String remark, List<Map<String, Object>> items) {
        PurchaseOrderMain main = new PurchaseOrderMain();
        main.setOrderNo(orderNo);
        main.setDocName(docName);
        main.setSupplierCode(supplierCode);
        main.setSupplierName(supplierName);
        main.setPaymentType(paymentType);
        main.setCurrencyCode(currencyCode);
        main.setExchangeRate(exchangeRate);
        main.setTotalAmount(totalAmount);
        main.setTaxAmount(taxAmount);
        main.setDiscountAmount(discountAmount);
        main.setPayAmount(payAmount);
        main.setOrderDate(orderDate);
        main.setDeliveryDate(deliveryDate);
        main.setStatus(0);
        main.setRemark(remark);
        main.setDelFlag(0);
        main.setCreateTime(LocalDateTime.now());
        orderMainMapper.insert(main);

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setOrderId(main.getId());
                item.setGoodsId(getLong(itemMap, "goodsId"));
                item.setGoodsCode(getString(itemMap, "goodsCode"));
                item.setGoodsName(getString(itemMap, "goodsName"));
                item.setGoodsSpec(getString(itemMap, "goodsSpec"));
                item.setSkuCode(getString(itemMap, "skuCode"));
                item.setSkuName(getString(itemMap, "skuName"));
                item.setUnitCode(getString(itemMap, "unitCode"));
                item.setConversionRate(getBigDecimal(itemMap, "conversionRate", BigDecimal.ONE));
                item.setQuantity(getBigDecimal(itemMap, "quantity", BigDecimal.ZERO));
                item.setUnitPrice(getBigDecimal(itemMap, "unitPrice", BigDecimal.ZERO));
                item.setTaxRate(getBigDecimal(itemMap, "taxRate", BigDecimal.ZERO));
                item.setDiscountRate(getBigDecimal(itemMap, "discountRate", BigDecimal.ZERO));
                item.setAmount(getBigDecimal(itemMap, "amount", BigDecimal.ZERO));
                item.setRemark(getString(itemMap, "remark"));
                orderItemMapper.insert(item);
            }
        }

        log.info("采购订单已保存: orderNo={}", orderNo);
        return main;
    }

    @Override
    @Transactional
    public PurchaseOrderMain audit(Long id, Integer status) {
        PurchaseOrderMain main = orderMainMapper.selectById(id);
        if (main != null) {
            main.setStatus(status);
            main.setAuditTime(LocalDateTime.now());
            orderMainMapper.updateById(main);
            log.info("采购订单已审核: id={}, status={}", id, status);
        }
        return main;
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString()); } catch (Exception e) { return null; }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key, BigDecimal defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try { return new BigDecimal(value.toString()); } catch (Exception e) { return defaultValue; }
    }
}
