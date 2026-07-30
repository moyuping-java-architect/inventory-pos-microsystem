package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SupplierQueryDTO implements Serializable {

    private String supplierCode;

    private String supplierName;

    private String shortName;

    private String contactName;

    private String supplierType;

    private String industry;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}