package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StockLossItemDTO implements Serializable {

    private Long id;
    private String goodsCode;
    private String skuCode;
    private String skuName;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private BigDecimal lossQuantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private String lossReason;
}