package com.psi.finance.dto;

import lombok.Data;

@Data
public class FinanceReceivableQueryDTO {
    private String storeCode;
    private String customerCode;
    private String customerName;
    private String billDateStart;
    private String billDateEnd;
    private Integer pageNum;
    private Integer pageSize;
}