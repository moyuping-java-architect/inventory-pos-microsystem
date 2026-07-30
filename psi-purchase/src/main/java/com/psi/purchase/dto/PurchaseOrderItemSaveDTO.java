package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemSaveDTO implements Serializable {

    private Integer itemNo;

    private Long goodsId;

    private String goodsCode;

    private String skuCode;

    private String skuName;

    private String goodsName;

    private String goodsSpec;

    private String unitCode;

    private String goodsUnit;

    private BigDecimal conversionRate;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal taxRate;

    private Integer isTaxInclusive;

    private BigDecimal discountRate;

    private String remark;
}