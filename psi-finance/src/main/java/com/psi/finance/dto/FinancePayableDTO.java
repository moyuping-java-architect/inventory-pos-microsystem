package com.psi.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinancePayableDTO {
    private Long id;
    private String storeCode;
    private String storeName;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainAmount;
    private String sourceNo;
    private String sourceType;
    private String billDate;
    private String dueDate;
    private LocalDateTime createTime;
}