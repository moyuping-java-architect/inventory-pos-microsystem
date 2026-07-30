package com.psi.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FinanceReceivablePayDTO {
    private Long id;
    private String storeCode;
    private String storeName;
    private Long receivableId;
    private String customerCode;
    private String customerName;
    private BigDecimal payAmount;
    private String payMethod;
    private String payNo;
    private String payDate;
    private String remark;
    private LocalDateTime createTime;
}