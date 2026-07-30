package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseInItemDTO implements Serializable {

    private Long id;

    private Long inId;

    private String inNo;

    private Long orderId;

    private String orderNo;

    private Integer itemNo;

    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String unitCode;

    private BigDecimal orderQuantity;

    private BigDecimal inQuantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private BigDecimal taxRate;

    private BigDecimal taxAmount;

    private String batchNo;

    private String expireDate;

    private String remark;
}