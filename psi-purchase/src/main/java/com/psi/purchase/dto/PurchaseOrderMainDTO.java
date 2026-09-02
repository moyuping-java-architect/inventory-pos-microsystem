package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderMainDTO implements Serializable {

    private Long id;

    private String orderNo;

    private String docName;

    private Long supplierId;

    private String supplierCode;

    private String supplierName;

    private String orderDate;

    private String deliveryDate;

    private Integer paymentType;

    private String currencyCode;

    private BigDecimal exchangeRate;

    private BigDecimal totalAmount;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private Integer orderStatus;

    private String remark;

    private Integer auditStatus;

    private LocalDateTime auditTime;

    private Long auditBy;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<PurchaseOrderItemDTO> items;
}