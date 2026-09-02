package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerQueryDTO implements Serializable {

    private String customerCode;
    private String customerName;
    private String shortName;
    private String contactName;
    private String contactPhone;
    private String customerType;
    private String customerLevel;
    private Integer status;
    private Integer pageNum;
    private Integer pageSize;
}