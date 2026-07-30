package com.trademaster.stock.service;

import com.trademaster.stock.entity.StockLossMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StockLossMainService {

    StockLossMain save(String lossNo, String docName, String warehouseCode, String warehouseName,
                       LocalDateTime lossDate, BigDecimal totalAmount, String lossReason,
                       String remark, List<Map<String, Object>> items);

    StockLossMain audit(Long id, Integer status);
}
