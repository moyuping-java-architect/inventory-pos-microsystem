package com.psi.cashier.service;

import com.psi.cashier.dto.MobileMoneyResult;

import java.math.BigDecimal;

/**
 * Mobile Money 收款服务
 */
public interface MobileMoneyService {

    /**
     * 发起收款
     *
     * @param provider 运营商（AIRTEL/MTN/ZAMTEL）
     * @param phone    手机号
     * @param amount   金额
     * @param orderNo  订单号
     * @return 收款结果
     */
    MobileMoneyResult collect(String provider, String phone, BigDecimal amount, String orderNo);

    /**
     * 撤销/退款已成功的收款（用于订单保存失败时的补偿）
     *
     * @param provider      运营商（AIRTEL/MTN/ZAMTEL）
     * @param transactionNo 原交易流水号
     * @param orderNo       订单号
     * @return 撤销结果
     */
    MobileMoneyResult reverse(String provider, String transactionNo, String orderNo);
}
