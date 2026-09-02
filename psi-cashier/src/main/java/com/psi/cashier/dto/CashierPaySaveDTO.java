package com.psi.cashier.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收银支付明细保存DTO
 * 支持多种支付方式组合支付
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class CashierPaySaveDTO {

    /**
     * 支付方式ID
     */
    private Integer payId;

    /**
     * 支付方式编码
     */
    private String payCode;

    /**
     * 支付方式名称（如：现金、微信、支付宝等）
     */
    private String payName;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付单号（如：微信支付单号、支付宝交易号等）
     */
    private String payNo;

    /**
     * 支付渠道（如：WECHAT、ALIPAY、CASH、AIRTEL、MTN、ZAMTEL等）
     */
    private String payChannel;

    /**
     * Mobile Money 运营商（AIRTEL/MTN/ZAMTEL）
     */
    private String mobileProvider;

    /**
     * Mobile Money 手机号
     */
    private String mobilePhone;

    /**
     * Mobile Money 交易流水号
     */
    private String mobileTransactionNo;

    /**
     * 支付币种（ZMW/USD）
     */
    private String currency;
}