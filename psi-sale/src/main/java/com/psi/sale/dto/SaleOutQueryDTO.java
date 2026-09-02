package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SaleOutQueryDTO implements Serializable {

    private String outNo;
    private String orderNo;
    private String customerCode;
    private String customerName;
    private String outDate;
    private String warehouseCode;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}