package com.psi.cashier.dto;

import lombok.Data;

/**
 * 收银查询条件 DTO
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class CashierMainQueryDTO {

    /**
     * 收银单号
     */
    private String cashierNo;

    /**
     * 店铺编码
     */
    private String storeCode;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;
}