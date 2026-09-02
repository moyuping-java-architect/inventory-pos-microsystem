package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SaleOutSaveDTO implements Serializable {

    private String docName;

    private String outNo;

    private String orderNo;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String outDate;
    private String warehouseCode;
    private String warehouseName;
    private String remark;
    private List<SaleOutItemSaveDTO> items;
}