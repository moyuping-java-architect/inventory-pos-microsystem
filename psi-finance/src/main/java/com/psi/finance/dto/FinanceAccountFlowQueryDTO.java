package com.psi.finance.dto;

import lombok.Data;

@Data
public class FinanceAccountFlowQueryDTO {
    private String storeCode;
    private String accountType;
    private Integer flowType;
    private String sourceNo;
    private String startDate;
    private String endDate;
    private Integer pageNum;
    private Integer pageSize;
}