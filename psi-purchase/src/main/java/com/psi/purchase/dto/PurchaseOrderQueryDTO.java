package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PurchaseOrderQueryDTO implements Serializable {

    private String orderNo;

    private String supplierCode;

    private String supplierName;

    private String orderDate;

    private String startDate;

    private String endDate;

    private Integer orderStatus;

    private Integer auditStatus;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}