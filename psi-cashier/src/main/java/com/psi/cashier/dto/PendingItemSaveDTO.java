package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 挂单明细保存DTO
 * 用于挂单明细的保存请求
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class PendingItemSaveDTO {

    private String skuId;

    private String skuCode;

    private String barCode;

    private String productName;

    private String saleUnitName;

    private BigDecimal saleQuantity;

    private BigDecimal unitPrice;

    private BigDecimal memberPrice;

    private BigDecimal subtotal;
}