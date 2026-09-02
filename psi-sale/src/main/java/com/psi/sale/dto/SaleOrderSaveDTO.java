package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleOrderSaveDTO implements Serializable {

    private String docName;

    private String orderNo;

    private Long customerId;
    private String customerCode;
    private String customerName;
    private String orderDate;
    private String deliveryDate;
    private Integer saleType;
    private Integer paymentType;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String warehouseCode;
    private String warehouseName;
    private String remark;
    private List<SaleOrderItemSaveDTO> items;
}