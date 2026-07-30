package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleReturnMainDTO implements Serializable {

    private Long id;
    private String returnNo;
    private String docName;
    private String outNo;
    private String orderNo;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String returnDate;
    private String warehouseCode;
    private String warehouseName;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal payAmount;
    private String returnReason;
    private String remark;
    private Integer status;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SaleReturnItemDTO> items;
}