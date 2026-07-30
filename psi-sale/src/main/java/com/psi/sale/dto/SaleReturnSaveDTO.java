package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SaleReturnSaveDTO implements Serializable {

    private String docName;

    private String returnNo;

    private String outNo;
    private String orderNo;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String returnDate;
    private String warehouseCode;
    private String warehouseName;
    private String returnReason;
    private String remark;
    private List<SaleReturnItemSaveDTO> items;
}