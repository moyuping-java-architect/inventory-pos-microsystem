package com.trademaster.stock.service;

import com.trademaster.stock.entity.StockOverflowMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StockOverflowMainService {

    StockOverflowMain save(String overflowNo, String docName, String warehouseCode, String warehouseName,
                           LocalDateTime overflowDate, BigDecimal totalAmount, String overflowReason,
                           String remark, List<Map<String, Object>> items);

    StockOverflowMain audit(Long id, Integer status);
}
