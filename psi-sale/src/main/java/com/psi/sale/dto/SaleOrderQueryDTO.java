package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SaleOrderQueryDTO implements Serializable {

    private String orderNo;
    private String customerCode;
    private String customerName;
    private String orderDate;
    private Integer orderStatus;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}