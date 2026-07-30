package com.trademaster.purchase.service;

import com.trademaster.purchase.entity.PurchaseInMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PurchaseInMainService {

    PurchaseInMain save(String inNo, String docName, String supplierCode, String supplierName,
                        String warehouseCode, String warehouseName, String orderNo,
                        LocalDateTime inDate, BigDecimal totalAmount, String remark,
                        List<Map<String, Object>> items);

    PurchaseInMain audit(Long id, Integer status);
}
