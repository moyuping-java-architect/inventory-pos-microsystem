package com.psi.sale.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SaleReturnItemDTO implements Serializable {

    private Long id;
    private Long returnId;
    private String returnNo;
    private Long outId;
    private String outNo;
    private Integer itemNo;
    private Long goodsId;
    private String goodsCode;
    private String goodsName;
    private String goodsSpec;
    private String unitCode;
    private BigDecimal outQuantity;
    private BigDecimal returnQuantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private String batchNo;
    private String expireDate;
    private String remark;
}