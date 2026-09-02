package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerDebtDetailQueryDTO implements Serializable {

    private Long customerId;
    private String customerCode;
    private String customerName;
    private Integer billType;
    private String billNo;
    private Integer status;
    private Integer pageNum;
    private Integer pageSize;
}