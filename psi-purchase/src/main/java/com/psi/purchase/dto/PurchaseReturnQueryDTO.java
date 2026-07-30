package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PurchaseReturnQueryDTO implements Serializable {

    private String returnNo;

    private String inNo;
    private String orderNo;

    private String supplierCode;

    private String supplierName;

    private String returnDate;

    private Integer returnStatus;

    private Integer auditStatus;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}