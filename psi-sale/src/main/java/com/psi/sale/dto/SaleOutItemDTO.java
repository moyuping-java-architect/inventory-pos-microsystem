package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SaleOutItemDTO implements Serializable {

    private Long id;
    private Long outId;
    private String outNo;
    private Long orderId;
    private String orderNo;
    private Integer itemNo;
    private Long goodsId;
    private String goodsCode;
    private String goodsName;
    private String goodsSpec;
    private String unitCode;
    private BigDecimal orderQuantity;
    private BigDecimal outQuantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private String batchNo;
    private String expireDate;
    private String remark;
}