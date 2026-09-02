package com.psi.purchase.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseReturnItemDTO implements Serializable {

    private Long id;

    private Long returnId;

    private String returnNo;

    private Long inId;

    private String inNo;
    private String orderNo;

    private Integer itemNo;

    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String unitCode;

    private BigDecimal inQuantity;

    private BigDecimal returnQuantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private BigDecimal taxRate;

    private BigDecimal taxAmount;

    private String batchNo;

    private String expireDate;

    private String remark;
}