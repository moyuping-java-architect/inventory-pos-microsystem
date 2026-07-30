package com.trademaster.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleOrderItemDTO {
    private Long goodsId;
    private Long skuId;
    private BigDecimal qty;
    private BigDecimal price;
    private BigDecimal amount;
}
