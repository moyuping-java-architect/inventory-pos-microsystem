package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseReturnSaveDTO implements Serializable {

    private String docName;

    private String returnNo;

    private String inNo;
    private String orderNo;

    private Long supplierId;

    private String supplierCode;

    private String supplierName;

    private String returnDate;

    private String warehouseCode;

    private String warehouseName;

    private String returnReason;

    private String remark;

    private List<PurchaseReturnItemSaveDTO> items;
}