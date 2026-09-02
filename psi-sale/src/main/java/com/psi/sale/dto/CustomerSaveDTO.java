package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CustomerSaveDTO implements Serializable {

    private String customerCode;
    private String customerName;
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
    private String customerType;
    private String customerLevel;
    private BigDecimal creditLimit;
    private String remark;
}