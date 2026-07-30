package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StockCheckItemSaveDTO implements Serializable {

    private Long id;
    private Long goodsId;
    private String goodsCode;
    private String skuCode;
    private String skuName;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private BigDecimal conversionRate;
    private BigDecimal bookQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal unitPrice;
}