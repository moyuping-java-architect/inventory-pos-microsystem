package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseReturnMainDTO implements Serializable {

    private Long id;

    private String returnNo;

    private String docName;

    private String inNo;
    private String orderNo;

    private Long supplierId;

    private String supplierCode;

    private String supplierName;

    private String returnDate;

    private String warehouseCode;

    private String warehouseName;

    private BigDecimal totalAmount;

    private BigDecimal taxAmount;

    private BigDecimal payAmount;

    private Integer returnStatus;

    private String returnReason;

    private String remark;

    private Integer auditStatus;

    private LocalDateTime auditTime;

    private Long auditBy;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<PurchaseReturnItemDTO> items;
}