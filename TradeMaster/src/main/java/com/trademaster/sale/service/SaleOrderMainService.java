package com.trademaster.sale.service;

import com.trademaster.sale.entity.SaleOrderMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SaleOrderMainService {

    SaleOrderMain save(String orderNo, String docName, String customerCode, String customerName,
                       Integer paymentType, String currencyCode, BigDecimal exchangeRate,
                       BigDecimal totalAmount, BigDecimal taxAmount, BigDecimal discountAmount,
                       BigDecimal payAmount, BigDecimal paidAmount, LocalDateTime orderDate,
                       LocalDateTime deliveryDate, String remark, List<Map<String, Object>> items);

    SaleOrderMain audit(Long id, Integer status);
}
