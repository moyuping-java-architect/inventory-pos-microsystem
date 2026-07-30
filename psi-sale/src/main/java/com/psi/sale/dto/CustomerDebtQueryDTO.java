package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerDebtQueryDTO implements Serializable {

    private String customerCode;
    private String customerName;
    private Integer pageNum;
    private Integer pageSize;
}