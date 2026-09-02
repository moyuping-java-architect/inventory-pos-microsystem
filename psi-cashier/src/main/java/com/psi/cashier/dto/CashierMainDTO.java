package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收银主表 DTO
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class CashierMainDTO {

    private Long id;

    /**
     * 收银单号
     */
    private String cashierNo;

    /**
     * 店铺编码
     */
    private String storeCode;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实收金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 支付单号
     */
    private String payNo;

    /**
     * 状态（0-待支付，1-已支付，2-已取消）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 明细列表
     */
    private List<CashierItemDTO> items;
}