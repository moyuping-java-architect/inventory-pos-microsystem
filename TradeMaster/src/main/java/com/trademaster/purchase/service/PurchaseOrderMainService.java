package com.trademaster.purchase.service;

import com.trademaster.purchase.entity.PurchaseOrderMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PurchaseOrderMainService {

    PurchaseOrderMain save(String orderNo, String docName, String supplierCode, String supplierName,
                           Integer paymentType, String currencyCode, BigDecimal exchangeRate,
                           BigDecimal totalAmount, BigDecimal taxAmount, BigDecimal discountAmount,
                           BigDecimal payAmount, LocalDateTime orderDate, LocalDateTime deliveryDate,
                           String remark, List<Map<String, Object>> items);

    PurchaseOrderMain audit(Long id, Integer status);
}
