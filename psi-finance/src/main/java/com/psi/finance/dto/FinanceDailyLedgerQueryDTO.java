package com.psi.finance.dto;

import lombok.Data;

@Data
public class FinanceDailyLedgerQueryDTO {
    private String storeCode;
    private String ledgerDateStart;
    private String ledgerDateEnd;
    private Integer pageNum;
    private Integer pageSize;
}