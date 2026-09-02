package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseInSaveDTO implements Serializable {

    private String docName;

    private String inNo;

    private String orderNo;

    private Long supplierId;

    private String supplierCode;

    private String supplierName;

    private String inDate;

    private String warehouseCode;

    private String warehouseName;

    private String remark;

    private List<PurchaseInItemSaveDTO> items;
}