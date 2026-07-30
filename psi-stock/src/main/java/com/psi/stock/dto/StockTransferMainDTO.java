package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockTransferMainDTO implements Serializable {

    private Long id;
    private String transferNo;
    private String docName;
    private String fromWarehouseCode;
    private String fromWarehouseName;
    private String toWarehouseCode;
    private String toWarehouseName;
    private String transferDate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String remark;
    private Integer status;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<StockTransferItemDTO> items;
}