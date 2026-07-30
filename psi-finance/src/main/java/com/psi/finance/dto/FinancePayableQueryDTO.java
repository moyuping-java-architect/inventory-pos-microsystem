package com.psi.finance.dto;

import lombok.Data;

@Data
public class FinancePayableQueryDTO {
    private String storeCode;
    private String supplierCode;
    private String supplierName;
    private String billDateStart;
    private String billDateEnd;
    private Integer pageNum;
    private Integer pageSize;
}