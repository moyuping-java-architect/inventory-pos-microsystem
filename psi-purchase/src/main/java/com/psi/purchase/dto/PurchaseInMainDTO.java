package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseInMainDTO implements Serializable {

    private Long id;

    private String inNo;

    private String docName;

    private String orderNo;

    private Long supplierId;

    private String supplierCode;

    private String supplierName;

    private String inDate;

    private String warehouseCode;

    private String warehouseName;

    private BigDecimal totalAmount;

    private BigDecimal taxAmount;

    private BigDecimal payAmount;

    private Integer inStatus;

    private String remark;

    private Integer auditStatus;

    private LocalDateTime auditTime;

    private Long auditBy;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<PurchaseInItemDTO> items;
}