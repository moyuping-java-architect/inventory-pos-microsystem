package com.psi.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinanceReceivableDTO {
    private Long id;
    private String storeCode;
    private String storeName;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainAmount;
    private String sourceNo;
    private String sourceType;
    private String billDate;
    private String dueDate;
    private LocalDateTime createTime;
}