package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemDTO implements Serializable {

    private Long id;

    private Long orderId;

    private String orderNo;

    private Integer itemNo;

    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String unitCode;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private BigDecimal taxRate;

    private Integer isTaxInclusive;

    private BigDecimal taxAmount;

    private BigDecimal discountRate;

    private BigDecimal discountAmount;

    private BigDecimal netAmount;

    private String remark;
}