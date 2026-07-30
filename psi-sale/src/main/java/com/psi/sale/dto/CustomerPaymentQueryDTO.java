package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerPaymentQueryDTO implements Serializable {

    private String paymentNo;
    private String customerCode;
    private String customerName;
    private String paymentDate;
    private Integer paymentMethod;
    private Integer pageNum;
    private Integer pageSize;
}