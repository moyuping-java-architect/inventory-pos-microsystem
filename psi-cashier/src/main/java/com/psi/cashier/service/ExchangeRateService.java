package com.psi.cashier.service;

import com.psi.cashier.entity.ExchangeRateEntity;

import java.math.BigDecimal;

/**
 * 汇率服务
 */
public interface ExchangeRateService {

    /**
     * 获取最新有效汇率
     *
     * @param from 源币种
     * @param to   目标币种
     * @return 汇率
     */
    BigDecimal getEffectiveRate(String from, String to);

    /**
     * 保存或更新汇率
     */
    void upsertRate(ExchangeRateEntity rate);
}
