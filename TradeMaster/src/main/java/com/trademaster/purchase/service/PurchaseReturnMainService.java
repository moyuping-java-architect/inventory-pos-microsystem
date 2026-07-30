package com.trademaster.purchase.service;

import com.trademaster.purchase.entity.PurchaseReturnMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PurchaseReturnMainService {

    PurchaseReturnMain save(String returnNo, String docName, String supplierCode, String supplierName,
                            String warehouseCode, String warehouseName, String orderNo,
                            LocalDateTime returnDate, BigDecimal totalAmount, String returnReason,
                            String remark, List<Map<String, Object>> items);

    PurchaseReturnMain audit(Long id, Integer status);
}
