package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleOrderMainDTO implements Serializable {

    private Long id;
    private String orderNo;
    private String docName;
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
    private String remark;
    private String warehouseCode;
    private String warehouseName;
    private Integer status;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SaleOrderItemDTO> items;
}