package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退货明细保存DTO
 * 用于退货明细的保存请求
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class RefundItemSaveDTO {

    private String skuId;

    private String skuCode;

    private String barCode;

    private String productName;

    private String saleUnitName;

    private BigDecimal refundQuantity;

    private BigDecimal refundPrice;

    private BigDecimal subtotal;

    private BigDecimal taxRate;

    private Integer isTaxInclusive;

    private BigDecimal netAmount;

    private BigDecimal taxAmount;

    private String batchNo;

    private String currency;
}