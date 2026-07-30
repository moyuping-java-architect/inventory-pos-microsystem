package com.trademaster.stock.service;

import com.trademaster.stock.entity.StockTransferMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StockTransferMainService {

    StockTransferMain save(String transferNo, String docName, String fromWarehouseCode, String fromWarehouseName,
                           String toWarehouseCode, String toWarehouseName, LocalDateTime transferDate,
                           BigDecimal totalAmount, String remark, List<Map<String, Object>> items);

    StockTransferMain audit(Long id, Integer status);
}
