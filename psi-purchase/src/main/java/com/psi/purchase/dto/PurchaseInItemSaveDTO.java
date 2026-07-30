package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseInItemSaveDTO implements Serializable {

    private Long orderId;

    private String orderNo;

    private Integer itemNo;

    private Long goodsId;

    private String goodsCode;

    private String skuCode;

    private String skuName;

    private String goodsName;

    private String goodsSpec;

    private String unitCode;

    private BigDecimal conversionRate;

    private BigDecimal orderQuantity;

    private BigDecimal inQuantity;

    private BigDecimal unitPrice;

    private BigDecimal taxRate;

    private String batchNo;

    private String expireDate;

    private String remark;
}