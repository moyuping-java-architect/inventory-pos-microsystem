package com.psi.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinancePaySaveDTO {
    private String storeCode;
    private Long targetId;
    private BigDecimal payAmount;
    private String payMethod;
    private String payNo;
    private String payDate;
    private String remark;
}