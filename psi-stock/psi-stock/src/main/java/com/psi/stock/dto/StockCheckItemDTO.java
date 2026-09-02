package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StockCheckItemDTO implements Serializable {

    private Long id;
    private String goodsCode;
    private String skuCode;
    private String skuName;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private BigDecimal bookQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal diffQuantity;
    private BigDecimal unitPrice;
    private BigDecimal bookAmount;
    private BigDecimal actualAmount;
    private BigDecimal diffAmount;
}