package com.trademaster.sale.service;

import com.trademaster.sale.entity.SaleReturnMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SaleReturnMainService {

    SaleReturnMain save(String returnNo, String docName, String customerCode, String customerName,
                        String warehouseCode, String warehouseName, String orderNo,
                        LocalDateTime returnDate, BigDecimal totalAmount, String returnReason,
                        String remark, List<Map<String, Object>> items);

    SaleReturnMain audit(Long id, Integer status);
}
