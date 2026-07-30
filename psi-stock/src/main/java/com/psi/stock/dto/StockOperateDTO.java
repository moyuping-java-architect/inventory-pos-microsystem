package com.psi.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockOperateDTO {

    private String warehouseCode;
    private String goodsCode;
    private String skuCode;
    private BigDecimal quantity;
    private BigDecimal costPrice;
    private String sourceNo;
    private String sourceType;
    private String remark;
}
