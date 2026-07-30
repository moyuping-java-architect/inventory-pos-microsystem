package com.trademaster.stock.service.impl;

import com.trademaster.stock.entity.StockTransferItem;
import com.trademaster.stock.entity.StockTransferMain;
import com.trademaster.stock.mapper.StockTransferItemMapper;
import com.trademaster.stock.mapper.StockTransferMainMapper;
import com.trademaster.stock.service.StockTransferMainService;
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
public class StockTransferMainServiceImpl implements StockTransferMainService {

    private final StockTransferMainMapper transferMainMapper;
    private final StockTransferItemMapper transferItemMapper;
    private final StockService stockService;

    public StockTransferMainServiceImpl(StockTransferMainMapper transferMainMapper, StockTransferItemMapper transferItemMapper, StockService stockService) {
        this.transferMainMapper = transferMainMapper;
        this.transferItemMapper = transferItemMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public StockTransferMain save(String transferNo, String docName, String fromWarehouseCode, String fromWarehouseName,
                                  String toWarehouseCode, String toWarehouseName, LocalDateTime transferDate,
                                  BigDecimal totalAmount, String remark, List<Map<String, Object>> items) {
        StockTransferMain main = new StockTransferMain();
        main.setTransferNo(transferNo);
        main.setDocName(docName);
        main.setFromWarehouseCode(fromWarehouseCode);
        main.setFromWarehouseName(fromWarehouseName);
        main.setToWarehouseCode(toWarehouseCode);
        main.setToWarehouseName(toWarehouseName);
        main.setTransferDate(transferDate);
        main.setTotalAmount(totalAmount);
        main.setStatus(0);
        main.setRemark(remark);
        main.setDelFlag(0);
        main.setCreateTime(LocalDateTime.now());
        transferMainMapper.insert(main);

        if (items != null) {
            for (Map<String, Object> itemMap : items) {
                StockTransferItem item = new StockTransferItem();
                item.setTransferId(main.getId());
                item.setGoodsId(getLong(itemMap, "goodsId"));
                item.setGoodsCode(getString(itemMap, "goodsCode"));
                item.setGoodsName(getString(itemMap, "goodsName"));
                item.setGoodsSpec(getString(itemMap, "goodsSpec"));
                item.setSkuCode(getString(itemMap, "skuCode"));
                item.setSkuName(getString(itemMap, "skuName"));
                item.setUnitCode(getString(itemMap, "unitCode"));
                item.setTransferQuantity(getBigDecimal(itemMap, "transferQuantity", BigDecimal.ZERO));
                item.setUnitCostPrice(getBigDecimal(itemMap, "unitCostPrice", BigDecimal.ZERO));
                item.setAmount(getBigDecimal(itemMap, "amount", BigDecimal.ZERO));
                item.setBatchNo(getString(itemMap, "batchNo"));
                item.setExpireDate(parseDate(getString(itemMap, "expireDate")));
                item.setRemark(getString(itemMap, "remark"));
                transferItemMapper.insert(item);
            }
        }

        log.info("调拨单已保存: transferNo={}", transferNo);
        return main;
    }

    @Override
    @Transactional
    public StockTransferMain audit(Long id, Integer status) {
        StockTransferMain main = transferMainMapper.selectById(id);
        if (main != null) {
            main.setStatus(status);
            main.setAuditTime(LocalDateTime.now());
            transferMainMapper.updateById(main);
            log.info("调拨单已审核: id={}, status={}", id, status);

            if (status >= 2) {
                List<StockTransferItem> items = transferItemMapper.selectByTransferId(id);
                for (StockTransferItem item : items) {
                    stockService.decreaseStock(
                            main.getFromWarehouseCode(),
                            item.getGoodsCode(),
                            item.getSkuCode(),
                            item.getTransferQuantity(),
                            main.getTransferNo(),
                            "STOCK_TRANSFER_OUT"
                    );
                    stockService.increaseStock(
                            main.getToWarehouseCode(),
                            item.getGoodsCode(),
                            item.getSkuCode(),
                            item.getTransferQuantity(),
                            item.getUnitCostPrice(),
                            item.getBatchNo(),
                            main.getTransferNo(),
                            "STOCK_TRANSFER_IN"
                    );
                }
                log.info("调拨库存已更新: transferNo={}, itemCount={}", main.getTransferNo(), items.size());
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
