package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockDTO implements Serializable {

    private Long id;
    private String warehouseCode;
    private String warehouseName;
    private String goodsCode;
    private String skuCode;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal lockedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal avgCostPrice;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}