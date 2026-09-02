package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收银明细 DTO
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class CashierItemDTO {

    private Long id;

    /**
     * 收银单ID
     */
    private Long cashierId;

    /**
     * 商品编码
     */
    private String goodsCode;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 仓库编码
     */
    private String warehouseCode;
}