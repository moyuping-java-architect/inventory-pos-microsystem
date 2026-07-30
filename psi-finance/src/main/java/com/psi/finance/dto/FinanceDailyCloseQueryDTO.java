package com.psi.finance.dto;

import lombok.Data;

@Data
public class FinanceDailyCloseQueryDTO {
    private String storeCode;
    private String closeDateStart;
    private String closeDateEnd;
    private Integer closeStatus;
    private Integer pageNum;
    private Integer pageSize;
}