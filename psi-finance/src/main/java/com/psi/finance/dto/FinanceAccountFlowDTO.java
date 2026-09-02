package com.psi.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinanceAccountFlowDTO {
    private Long id;
    private String storeCode;
    private String storeName;
    private String accountType;
    private String accountTypeName;
    private String accountName;
    private Integer flowType;
    private String flowTypeName;
    private BigDecimal inAmount;
    private BigDecimal outAmount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String sourceNo;
    private String sourceType;
    private String payNo;
    private String remark;
    private LocalDateTime createTime;
}