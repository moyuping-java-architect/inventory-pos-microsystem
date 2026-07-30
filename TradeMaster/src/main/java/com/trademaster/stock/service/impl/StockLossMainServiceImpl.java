package com.trademaster.stock.service.impl;

import com.trademaster.stock.entity.StockLossItem;
import com.trademaster.stock.entity.StockLossMain;
import com.trademaster.stock.mapper.StockLossItemMapper;
import com.trademaster.stock.mapper.StockLossMainMapper;
import com.trademaster.stock.service.StockLossMainService;
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
public class StockLossMainServiceImpl implements StockLossMainService {

    private final StockLossMainMapper lossMainMapper;
    private final StockLossItemMapper lossItemMapper;
    private final StockService stockService;

    public StockLossMainServiceImpl(StockLossMainMapper lossMainMapper, StockLossItemMapper lossItemMapper, StockService stockService) {
        this.lossMainMapper = lossMainMapper;
        this.lossItemMapper = lossItemMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public StockLossMain save(String lossNo, String docName, String warehouseCode, String warehouseName,
                              LocalDateTime lossDate, BigDecimal totalAmount, String lossReason,
                              String remark, List<Map<String, Object>> items) {
        StockLossMain main = new StockLossMain();
        main.setLossNo(lossNo);
        main.setDocName(docName);
        main.setWarehouseCode(warehouseCode);
        main.setWarehouseName(warehouseName);
        main.setLossDate(lossDate);
        main.setTotalAmount(totalAmount);
        main.setLossReason(lossReason);
        main.setStatus(0);
        main.setRemark(remark);
        main.setDelFlag(0);
        main.setCreateTime(LocalDateTime.now());
        lossMainMapper.insert(main);

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                StockLossItem item = new StockLossItem();
                item.setLossId(main.getId());
                item.setGoodsId(getLong(itemMap, "goodsId"));
                item.setGoodsCode(getString(itemMap, "goodsCode"));
                item.setGoodsName(getString(itemMap, "goodsName"));
                item.setGoodsSpec(getString(itemMap, "goodsSpec"));
                item.setSkuCode(getString(itemMap, "skuCode"));
                item.setSkuName(getString(itemMap, "skuName"));
                item.setUnitCode(getString(itemMap, "unitCode"));
                item.setLossQuantity(getBigDecimal(itemMap, "lossQuantity", BigDecimal.ZERO));
                item.setUnitCostPrice(getBigDecimal(itemMap, "unitCostPrice", BigDecimal.ZERO));
                item.setAmount(getBigDecimal(itemMap, "amount", BigDecimal.ZERO));
                item.setBatchNo(getString(itemMap, "batchNo"));
                item.setExpireDate(parseDate(getString(itemMap, "expireDate")));
                item.setRemark(getString(itemMap, "remark"));
                lossItemMapper.insert(item);
            }
        }

        log.info("报损单已保存: lossNo={}", lossNo);
        return main;
    }

    @Override
    @Transactional
    public StockLossMain audit(Long id, Integer status) {
        StockLossMain main = lossMainMapper.selectById(id);
        if (main != null) {
            main.setStatus(status);
            main.setAuditTime(LocalDateTime.now());
            lossMainMapper.updateById(main);
            log.info("报损单已审核: id={}, status={}", id, status);

            if (status >= 2) {
                List<StockLossItem> items = lossItemMapper.selectByLossId(id);
                for (StockLossItem item : items) {
                    stockService.decreaseStock(
                            main.getWarehouseCode(),
                            item.getGoodsCode(),
                            item.getSkuCode(),
                            item.getLossQuantity(),
                            main.getLossNo(),
                            "STOCK_LOSS"
                    );
                }
                log.info("报损库存已更新: lossNo={}, itemCount={}", main.getLossNo(), items.size());
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
