package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleOutMainDTO implements Serializable {

    private Long id;
    private String outNo;
    private String docName;
    private String orderNo;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String outDate;
    private String warehouseCode;
    private String warehouseName;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String remark;
    private Integer status;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SaleOutItemDTO> items;
}