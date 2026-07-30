package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SaleReturnQueryDTO implements Serializable {

    private String returnNo;
    private String outNo;
    private String orderNo;
    private String customerCode;
    private String customerName;
    private String returnDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}