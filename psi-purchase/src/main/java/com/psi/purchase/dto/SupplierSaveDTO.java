package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SupplierSaveDTO implements Serializable {

    private String supplierCode;

    private String supplierName;

    private String shortName;

    private String contactName;

    private String contactPhone;

    private String email;

    private String address;

    private String province;

    private String city;

    private String district;

    private String zipCode;

    private String taxNo;

    private String bankName;

    private String bankAccount;

    private String supplierType;

    private String industry;

    private String creditLevel;

    private String remark;
}