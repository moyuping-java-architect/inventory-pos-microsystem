package com.trademaster.sale.service.impl;

import com.trademaster.sale.entity.SaleOutItem;
import com.trademaster.sale.entity.SaleOutMain;
import com.trademaster.sale.mapper.SaleOutItemMapper;
import com.trademaster.sale.mapper.SaleOutMainMapper;
import com.trademaster.sale.service.SaleOutMainService;
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
public class SaleOutMainServiceImpl implements SaleOutMainService {

    private final SaleOutMainMapper outMainMapper;
    private final SaleOutItemMapper outItemMapper;
    private final StockService stockService;

    public SaleOutMainServiceImpl(SaleOutMainMapper outMainMapper, SaleOutItemMapper outItemMapper, StockService stockService) {
        this.outMainMapper = outMainMapper;
        this.outItemMapper = outItemMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public SaleOutMain save(String outNo, String docName, String customerCode, String customerName,
                            String warehouseCode, String warehouseName, String orderNo,
                            LocalDateTime outDate, BigDecimal totalAmount, String remark,
                            List<Map<String, Object>> items) {
        SaleOutMain main = new SaleOutMain();
        main.setOutNo(outNo);
        main.setDocName(docName);
        main.setCustomerCode(customerCode);
        main.setCustomerName(customerName);
        main.setWarehouseCode(warehouseCode);
        main.setWarehouseName(warehouseName);
        main.setOrderNo(orderNo);
        main.setOutDate(outDate);
        main.setTotalAmount(totalAmount);
        main.setStatus(0);
        main.setRemark(remark);
        main.setDelFlag(0);
        main.setCreateTime(LocalDateTime.now());
        outMainMapper.insert(main);

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                SaleOutItem item = new SaleOutItem();
                item.setOutId(main.getId());
                item.setGoodsId(getLong(itemMap, "goodsId"));
                item.setGoodsCode(getString(itemMap, "goodsCode"));
                item.setGoodsName(getString(itemMap, "goodsName"));
                item.setGoodsSpec(getString(itemMap, "goodsSpec"));
                item.setSkuCode(getString(itemMap, "skuCode"));
                item.setSkuName(getString(itemMap, "skuName"));
                item.setUnitCode(getString(itemMap, "unitCode"));
                item.setConversionRate(getBigDecimal(itemMap, "conversionRate", BigDecimal.ONE));
                item.setOutQuantity(getBigDecimal(itemMap, "outQuantity", BigDecimal.ZERO));
                item.setUnitPrice(getBigDecimal(itemMap, "unitPrice", BigDecimal.ZERO));
                item.setTaxRate(getBigDecimal(itemMap, "taxRate", BigDecimal.ZERO));
                item.setAmount(getBigDecimal(itemMap, "amount", BigDecimal.ZERO));
                item.setBatchNo(getString(itemMap, "batchNo"));
                item.setExpireDate(parseDate(getString(itemMap, "expireDate")));
                item.setRemark(getString(itemMap, "remark"));
                outItemMapper.insert(item);
            }
        }

        log.info("销售出库单已保存: outNo={}", outNo);
        return main;
    }

    @Override
    @Transactional
    public SaleOutMain audit(Long id, Integer status) {
        SaleOutMain main = outMainMapper.selectById(id);
        if (main != null) {
            main.setStatus(status);
            main.setAuditTime(LocalDateTime.now());
            outMainMapper.updateById(main);
            log.info("销售出库单已审核: id={}, status={}", id, status);

            if (status >= 2) {
                List<SaleOutItem> items = outItemMapper.selectByOutId(id);
                for (SaleOutItem item : items) {
                    stockService.decreaseStock(
                            main.getWarehouseCode(),
                            item.getGoodsCode(),
                            item.getSkuCode(),
                            item.getOutQuantity(),
                            main.getOutNo(),
                            "SALE_OUT"
                    );
                }
                log.info("销售出库库存已更新: outNo={}, itemCount={}", main.getOutNo(), items.size());
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
