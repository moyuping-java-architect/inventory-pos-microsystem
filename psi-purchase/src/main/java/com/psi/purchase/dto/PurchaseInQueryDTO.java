package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PurchaseInQueryDTO implements Serializable {

    private String inNo;

    private String orderNo;

    private String supplierCode;

    private String supplierName;

    private String inDate;

    private String warehouseCode;

    private Integer inStatus;

    private Integer auditStatus;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}