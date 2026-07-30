package com.trademaster.purchase.service.impl;

import com.trademaster.purchase.entity.PurchaseReturnItem;
import com.trademaster.purchase.entity.PurchaseReturnMain;
import com.trademaster.purchase.mapper.PurchaseReturnItemMapper;
import com.trademaster.purchase.mapper.PurchaseReturnMainMapper;
import com.trademaster.purchase.service.PurchaseReturnMainService;
import com.trademaster.stock.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PurchaseReturnMainServiceImpl implements PurchaseReturnMainService {

    private final PurchaseReturnMainMapper returnMainMapper;
    private final PurchaseReturnItemMapper returnItemMapper;
    private final StockService stockService;

    public PurchaseReturnMainServiceImpl(PurchaseReturnMainMapper returnMainMapper, PurchaseReturnItemMapper returnItemMapper, StockService stockService) {
        this.returnMainMapper = returnMainMapper;
        this.returnItemMapper = returnItemMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public PurchaseReturnMain save(String returnNo, String docName, String supplierCode, String supplierName,
                                   String warehouseCode, String warehouseName, String orderNo,
                                   LocalDateTime returnDate, BigDecimal totalAmount, String returnReason,
                                   String remark, List<Map<String, Object>> items) {
        PurchaseReturnMain main = new PurchaseReturnMain();
        main.setReturnNo(returnNo);
        main.setDocName(docName);
        main.setSupplierCode(supplierCode);
        main.setSupplierName(supplierName);
        main.setWarehouseCode(warehouseCode);
        main.setWarehouseName(warehouseName);
        main.setOrderNo(orderNo);
        main.setReturnDate(returnDate);
        main.setTotalAmount(totalAmount);
        main.setReturnReason(returnReason);
        main.setStatus(0);
        main.setRemark(remark);
        main.setDelFlag(0);
        main.setCreateTime(LocalDateTime.now());
        returnMainMapper.insert(main);

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                PurchaseReturnItem item = new PurchaseReturnItem();
                item.setReturnId(main.getId());
                item.setGoodsId(getLong(itemMap, "goodsId"));
                item.setGoodsCode(getString(itemMap, "goodsCode"));
                item.setGoodsName(getString(itemMap, "goodsName"));
                item.setGoodsSpec(getString(itemMap, "goodsSpec"));
                item.setSkuCode(getString(itemMap, "skuCode"));
                item.setSkuName(getString(itemMap, "skuName"));
                item.setUnitCode(getString(itemMap, "unitCode"));
                item.setConversionRate(getBigDecimal(itemMap, "conversionRate", BigDecimal.ONE));
                item.setReturnQuantity(getBigDecimal(itemMap, "returnQuantity", BigDecimal.ZERO));
                item.setUnitPrice(getBigDecimal(itemMap, "unitPrice", BigDecimal.ZERO));
                item.setTaxRate(getBigDecimal(itemMap, "taxRate", BigDecimal.ZERO));
                item.setAmount(getBigDecimal(itemMap, "amount", BigDecimal.ZERO));
                item.setBatchNo(getString(itemMap, "batchNo"));
                item.setExpireDate(parseDate(getString(itemMap, "expireDate")));
                item.setRemark(getString(itemMap, "remark"));
                returnItemMapper.insert(item);
            }
        }

        log.info("采购退货单已保存: returnNo={}", returnNo);
        return main;
    }

    @Override
    @Transactional
    public PurchaseReturnMain audit(Long id, Integer status) {
        PurchaseReturnMain main = returnMainMapper.selectById(id);
        if (main != null) {
            main.setStatus(status);
            main.setAuditTime(LocalDateTime.now());
            returnMainMapper.updateById(main);
            log.info("采购退货单已审核: id={}, status={}", id, status);

            if (status >= 2) {
                List<PurchaseReturnItem> items = returnItemMapper.selectByReturnId(id);
                for (PurchaseReturnItem item : items) {
                    stockService.decreaseStock(
                            main.getWarehouseCode(),
                            item.getGoodsCode(),
                            item.getSkuCode(),
                            item.getReturnQuantity(),
                            main.getReturnNo(),
                            "PURCHASE_RETURN"
                    );
                }
                log.info("采购退货库存已更新: returnNo={}, itemCount={}", main.getReturnNo(), items.size());
            }
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

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            if (dateStr.length() > 10) {
                dateStr = dateStr.substring(0, 10);
            }
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null;
        }
    }
}
