package com.psi.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinanceAccountDTO {
    private Long id;
    private String storeCode;
    private String storeName;
    private String accountType;
    private String accountTypeName;
    private String accountName;
    private String accountNo;
    private BigDecimal balance;
    private String remark;
    private LocalDateTime createTime;
}