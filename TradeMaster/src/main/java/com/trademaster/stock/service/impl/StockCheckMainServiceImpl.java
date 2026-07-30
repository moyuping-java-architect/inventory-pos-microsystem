package com.trademaster.stock.service.impl;

import com.trademaster.stock.entity.StockCheckItem;
import com.trademaster.stock.entity.StockCheckMain;
import com.trademaster.stock.mapper.StockCheckItemMapper;
import com.trademaster.stock.mapper.StockCheckMainMapper;
import com.trademaster.stock.service.StockCheckMainService;
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
public class StockCheckMainServiceImpl implements StockCheckMainService {

    private final StockCheckMainMapper checkMainMapper;
    private final StockCheckItemMapper checkItemMapper;
    private final StockService stockService;

    public StockCheckMainServiceImpl(StockCheckMainMapper checkMainMapper, StockCheckItemMapper checkItemMapper, StockService stockService) {
        this.checkMainMapper = checkMainMapper;
        this.checkItemMapper = checkItemMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public StockCheckMain save(String checkNo, String docName, String warehouseCode, String warehouseName,
                               LocalDateTime checkDate, BigDecimal varianceAmount, String remark,
                               List<Map<String, Object>> items) {
        StockCheckMain main = new StockCheckMain();
        main.setCheckNo(checkNo);
        main.setDocName(docName);
        main.setWarehouseCode(warehouseCode);
        main.setWarehouseName(warehouseName);
        main.setCheckDate(checkDate);
        main.setVarianceAmount(varianceAmount);
        main.setStatus(0);
        main.setRemark(remark);
        main.setDelFlag(0);
        main.setCreateTime(LocalDateTime.now());
        checkMainMapper.insert(main);

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                StockCheckItem item = new StockCheckItem();
                item.setCheckId(main.getId());
                item.setGoodsId(getLong(itemMap, "goodsId"));
                item.setGoodsCode(getString(itemMap, "goodsCode"));
                item.setGoodsName(getString(itemMap, "goodsName"));
                item.setGoodsSpec(getString(itemMap, "goodsSpec"));
                item.setSkuCode(getString(itemMap, "skuCode"));
                item.setSkuName(getString(itemMap, "skuName"));
                item.setUnitCode(getString(itemMap, "unitCode"));
                item.setSystemQuantity(getBigDecimal(itemMap, "systemQuantity", BigDecimal.ZERO));
                item.setActualQuantity(getBigDecimal(itemMap, "actualQuantity", BigDecimal.ZERO));
                item.setVarianceQuantity(getBigDecimal(itemMap, "varianceQuantity", BigDecimal.ZERO));
                item.setUnitCostPrice(getBigDecimal(itemMap, "unitCostPrice", BigDecimal.ZERO));
                item.setVarianceAmount(getBigDecimal(itemMap, "varianceAmount", BigDecimal.ZERO));
                item.setBatchNo(getString(itemMap, "batchNo"));
                item.setExpireDate(parseDate(getString(itemMap, "expireDate")));
                item.setRemark(getString(itemMap, "remark"));
                checkItemMapper.insert(item);
            }
        }

        log.info("盘点单已保存: checkNo={}", checkNo);
        return main;
    }

    @Override
    @Transactional
    public StockCheckMain audit(Long id, Integer status) {
        StockCheckMain main = checkMainMapper.selectById(id);
        if (main != null) {
            main.setStatus(status);
            main.setAuditTime(LocalDateTime.now());
            checkMainMapper.updateById(main);
            log.info("盘点单已审核: id={}, status={}", id, status);

            if (status >= 2) {
                List<StockCheckItem> items = checkItemMapper.selectByCheckId(id);
                for (StockCheckItem item : items) {
                    BigDecimal variance = item.getVarianceQuantity();
                    if (variance != null && variance.compareTo(BigDecimal.ZERO) != 0) {
                        if (variance.compareTo(BigDecimal.ZERO) > 0) {
                            stockService.increaseStock(
                                    main.getWarehouseCode(),
                                    item.getGoodsCode(),
                                    item.getSkuCode(),
                                    variance,
                                    item.getUnitCostPrice(),
                                    item.getBatchNo(),
                                    main.getCheckNo(),
                                    "STOCK_CHECK"
                            );
                        } else {
                            stockService.decreaseStock(
                                    main.getWarehouseCode(),
                                    item.getGoodsCode(),
                                    item.getSkuCode(),
                                    variance.negate(),
                                    main.getCheckNo(),
                                    "STOCK_CHECK"
                            );
                        }
                    }
                }
                log.info("盘点库存已更新: checkNo={}, itemCount={}", main.getCheckNo(), items.size());
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
