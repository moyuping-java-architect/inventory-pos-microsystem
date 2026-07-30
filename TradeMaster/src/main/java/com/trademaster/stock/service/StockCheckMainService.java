package com.trademaster.stock.service;

import com.trademaster.stock.entity.StockCheckMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StockCheckMainService {

    StockCheckMain save(String checkNo, String docName, String warehouseCode, String warehouseName,
                        LocalDateTime checkDate, BigDecimal varianceAmount, String remark,
                        List<Map<String, Object>> items);

    StockCheckMain audit(Long id, Integer status);
}
