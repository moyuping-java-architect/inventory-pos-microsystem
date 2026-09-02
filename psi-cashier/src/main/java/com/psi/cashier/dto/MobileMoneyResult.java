package com.psi.cashier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mobile Money 收款结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileMoneyResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 运营商
     */
    private String provider;

    /**
     * 收款手机号
     */
    private String phone;

    /**
     * 收款金额
     */
    private BigDecimal amount;

    public static MobileMoneyResult success(String provider, String phone, BigDecimal amount, String transactionNo) {
        return new MobileMoneyResult(true, transactionNo, "收款成功", provider, phone, amount);
    }

    public static MobileMoneyResult fail(String provider, String phone, BigDecimal amount, String message) {
        return new MobileMoneyResult(false, null, message, provider, phone, amount);
    }
}
