package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 小票商品项数据传输对象
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class ReceiptItemDTO {

    private String productName;

    private String barCode;

    private String skuCode;

    private String saleUnitName;

    private BigDecimal saleQuantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}
