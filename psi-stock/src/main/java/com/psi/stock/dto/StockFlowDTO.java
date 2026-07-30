package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockFlowDTO implements Serializable {

    private Long id;
    private String warehouseCode;
    private String warehouseName;
    private String goodsCode;
    private String skuCode;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private Integer flowType;
    private String flowTypeName;
    private BigDecimal inQuantity;
    private BigDecimal outQuantity;
    private BigDecimal beforeQuantity;
    private BigDecimal afterQuantity;
    private BigDecimal costPrice;
    private BigDecimal amount;
    private String sourceNo;
    private String sourceType;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
}