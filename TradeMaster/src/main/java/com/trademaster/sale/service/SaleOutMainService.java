package com.trademaster.sale.service;

import com.trademaster.sale.entity.SaleOutMain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SaleOutMainService {

    SaleOutMain save(String outNo, String docName, String customerCode, String customerName,
                     String warehouseCode, String warehouseName, String orderNo,
                     LocalDateTime outDate, BigDecimal totalAmount, String remark,
                     List<Map<String, Object>> items);

    SaleOutMain audit(Long id, Integer status);
}
