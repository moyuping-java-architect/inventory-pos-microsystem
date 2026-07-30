package com.trademaster.purchase.service.impl;

import com.trademaster.purchase.entity.PurchaseInItem;
import com.trademaster.purchase.entity.PurchaseInMain;
import com.trademaster.purchase.mapper.PurchaseInItemMapper;
import com.trademaster.purchase.mapper.PurchaseInMainMapper;
import com.trademaster.purchase.service.PurchaseInMainService;
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
public class PurchaseInMainServiceImpl implements PurchaseInMainService {

    private final PurchaseInMainMapper inMainMapper;
    private final PurchaseInItemMapper inItemMapper;
    private final StockService stockService;

    public PurchaseInMainServiceImpl(PurchaseInMainMapper inMainMapper, PurchaseInItemMapper inItemMapper, StockService stockService) {
        this.inMainMapper = inMainMapper;
        this.inItemMapper = inItemMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public PurchaseInMain save(String inNo, String docName, String supplierCode, String supplierName,
                               String warehouseCode, String warehouseName, String orderNo,
                               LocalDateTime inDate, BigDecimal totalAmount, String remark,
                               List<Map<String, Object>> items) {
        PurchaseInMain main = new PurchaseInMain();
        main.setInNo(inNo);
        main.setDocName(docName);
        main.setSupplierCode(supplierCode);
        main.setSupplierName(supplierName);
        main.setWarehouseCode(warehouseCode);
        main.setWarehouseName(warehouseName);
        main.setOrderNo(orderNo);
        main.setInDate(inDate);
        main.setTotalAmount(totalAmount);
        main.setStatus(0);
        main.setRemark(remark);
        main.setDelFlag(0);
        main.setCreateTime(LocalDateTime.now());
        inMainMapper.insert(main);

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                PurchaseInItem item = new PurchaseInItem();
                item.setInId(main.getId());
                item.setGoodsId(getLong(itemMap, "goodsId"));
                item.setGoodsCode(getString(itemMap, "goodsCode"));
                item.setGoodsName(getString(itemMap, "goodsName"));
                item.setGoodsSpec(getString(itemMap, "goodsSpec"));
                item.setSkuCode(getString(itemMap, "skuCode"));
                item.setSkuName(getString(itemMap, "skuName"));
                item.setUnitCode(getString(itemMap, "unitCode"));
                item.setConversionRate(getBigDecimal(itemMap, "conversionRate", BigDecimal.ONE));
                item.setInQuantity(getBigDecimal(itemMap, "inQuantity", BigDecimal.ZERO));
                item.setUnitPrice(getBigDecimal(itemMap, "unitPrice", BigDecimal.ZERO));
                item.setTaxRate(getBigDecimal(itemMap, "taxRate", BigDecimal.ZERO));
                item.setAmount(getBigDecimal(itemMap, "amount", BigDecimal.ZERO));
                item.setBatchNo(getString(itemMap, "batchNo"));
                item.setExpireDate(parseDate(getString(itemMap, "expireDate")));
                item.setRemark(getString(itemMap, "remark"));
                inItemMapper.insert(item);
            }
        }

        log.info("采购入库单已保存: inNo={}", inNo);
        return main;
    }

    @Override
    @Transactional
    public PurchaseInMain audit(Long id, Integer status) {
        PurchaseInMain main = inMainMapper.selectById(id);
        if (main != null) {
            main.setStatus(status);
            main.setAuditTime(LocalDateTime.now());
            inMainMapper.updateById(main);
            log.info("采购入库单已审核: id={}, status={}", id, status);

            if (status >= 2) {
                List<PurchaseInItem> items = inItemMapper.selectByInId(id);
                for (PurchaseInItem item : items) {
                    stockService.increaseStock(
                            main.getWarehouseCode(),
                            item.getGoodsCode(),
                            item.getSkuCode(),
                            item.getInQuantity(),
                            item.getUnitPrice(),
                            item.getBatchNo(),
                            main.getInNo(),
                            "PURCHASE_IN"
                    );
                }
                log.info("采购入库库存已更新: inNo={}, itemCount={}", main.getInNo(), items.size());
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
