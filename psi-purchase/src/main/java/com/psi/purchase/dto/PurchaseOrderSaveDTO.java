package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderSaveDTO implements Serializable {

    private String docName;

    private String orderNo;

    private Long supplierId;

    private String supplierCode;

    private String supplierName;

    private String orderDate;

    private String deliveryDate;

    private Integer paymentType;

    private String currencyCode;

    private BigDecimal exchangeRate;

    private String remark;

    private List<PurchaseOrderItemSaveDTO> items;
}