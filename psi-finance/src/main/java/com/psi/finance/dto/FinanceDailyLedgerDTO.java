package com.psi.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinanceDailyLedgerDTO {
    private Long id;
    private String storeCode;
    private String storeName;
    private String ledgerDate;
    private BigDecimal saleAmount;
    private BigDecimal costAmount;
    private BigDecimal profitAmount;
    private BigDecimal cashIn;
    private BigDecimal cashOut;
    private BigDecimal transferIn;
    private BigDecimal transferOut;
    private BigDecimal receivableAmount;
    private BigDecimal payableAmount;
    private BigDecimal beginningBalance;
    private BigDecimal endingBalance;
}