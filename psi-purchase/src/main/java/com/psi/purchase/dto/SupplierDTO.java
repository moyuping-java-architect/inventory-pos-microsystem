package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SupplierDTO implements Serializable {

    private Long id;

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

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}