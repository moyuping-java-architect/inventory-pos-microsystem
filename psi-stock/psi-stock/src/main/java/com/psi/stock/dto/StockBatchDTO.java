package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockBatchDTO implements Serializable {

    private Long id;
    private String warehouseCode;
    private String warehouseName;
    private String goodsCode;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private String batchNo;
    private String productionDate;
    private String expireDate;
    private BigDecimal quantity;
    private BigDecimal lockedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal costPrice;
    private BigDecimal totalAmount;
    private String supplierCode;
    private String supplierName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}