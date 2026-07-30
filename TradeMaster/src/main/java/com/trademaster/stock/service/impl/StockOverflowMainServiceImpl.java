package com.trademaster.stock.service.impl;

import com.trademaster.stock.entity.StockOverflowItem;
import com.trademaster.stock.entity.StockOverflowMain;
import com.trademaster.stock.mapper.StockOverflowItemMapper;
import com.trademaster.stock.mapper.StockOverflowMainMapper;
import com.trademaster.stock.service.StockOverflowMainService;
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
public class StockOverflowMainServiceImpl implements StockOverflowMainService {

    private final StockOverflowMainMapper overflowMainMapper;
    private final StockOverflowItemMapper overflowItemMapper;
    private final StockService stockService;

    public StockOverflowMainServiceImpl(StockOverflowMainMapper overflowMainMapper, StockOverflowItemMapper overflowItemMapper, StockService stockService) {
        this.overflowMainMapper = overflowMainMapper;
        this.overflowItemMapper = overflowItemMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public StockOverflowMain save(String overflowNo, String docName, String warehouseCode, String warehouseName,
                                  LocalDateTime overflowDate, BigDecimal totalAmount, String overflowReason,
                                  String remark, List<Map<String, Object>> items) {
        StockOverflowMain main = new StockOverflowMain();
        main.setOverflowNo(overflowNo);
        main.setDocName(docName);
        main.setWarehouseCode(warehouseCode);
        main.setWarehouseName(warehouseName);
        main.setOverflowDate(overflowDate);
        main.setTotalAmount(totalAmount);
        main.setOverflowReason(overflowReason);
        main.setStatus(0);
        main.setRemark(remark);
        main.setDelFlag(0);
        main.setCreateTime(LocalDateTime.now());
        overflowMainMapper.insert(main);

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                StockOverflowItem item = new StockOverflowItem();
                item.setOverflowId(main.getId());
                item.setGoodsId(getLong(itemMap, "goodsId"));
                item.setGoodsCode(getString(itemMap, "goodsCode"));
                item.setGoodsName(getString(itemMap, "goodsName"));
                item.setGoodsSpec(getString(itemMap, "goodsSpec"));
                item.setSkuCode(getString(itemMap, "skuCode"));
                item.setSkuName(getString(itemMap, "skuName"));
                item.setUnitCode(getString(itemMap, "unitCode"));
                item.setOverflowQuantity(getBigDecimal(itemMap, "overflowQuantity", BigDecimal.ZERO));
                item.setUnitCostPrice(getBigDecimal(itemMap, "unitCostPrice", BigDecimal.ZERO));
                item.setAmount(getBigDecimal(itemMap, "amount", BigDecimal.ZERO));
                item.setBatchNo(getString(itemMap, "batchNo"));
                item.setExpireDate(parseDate(getString(itemMap, "expireDate")));
                item.setRemark(getString(itemMap, "remark"));
                overflowItemMapper.insert(item);
            }
        }

        log.info("报溢单已保存: overflowNo={}", overflowNo);
        return main;
    }

    @Override
    @Transactional
    public StockOverflowMain audit(Long id, Integer status) {
        StockOverflowMain main = overflowMainMapper.selectById(id);
        if (main != null) {
            main.setStatus(status);
            main.setAuditTime(LocalDateTime.now());
            overflowMainMapper.updateById(main);
            log.info("报溢单已审核: id={}, status={}", id, status);

            if (status >= 2) {
                List<StockOverflowItem> items = overflowItemMapper.selectByOverflowId(id);
                for (StockOverflowItem item : items) {
                    stockService.increaseStock(
                            main.getWarehouseCode(),
                            item.getGoodsCode(),
                            item.getSkuCode(),
                            item.getOverflowQuantity(),
                            item.getUnitCostPrice(),
                            item.getBatchNo(),
                            main.getOverflowNo(),
                            "STOCK_OVERFLOW"
                    );
                }
                log.info("报溢库存已更新: overflowNo={}, itemCount={}", main.getOverflowNo(), items.size());
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
