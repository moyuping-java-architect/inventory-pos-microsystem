package com.psi.cashier.service;

import java.time.LocalDate;

/**
 * 日结校验服务
 * 提供日结状态校验功能
 */
public interface SettlementCheckService {

    /**
     * 检查是否可以进行销售
     * 规则：如果今天以前有未日结的日期，则不允许销售
     * 
     * @return true-可以销售，false-需要先日结
     */
    boolean canSell();

    /**
     * 获取未日结的日期（如果有）
     * 
     * @return 未日结的日期，如果所有日期都已日结则返回null
     */
    LocalDate getFirstUnsettledDate();

    /**
     * 获取未日结日期的字符串描述
     * 
     * @return 未日结日期描述，如"2026-06-08"，如果都已日结则返回null
     */
    String getUnsettledDateStr();

    /**
     * 更新日结状态缓存
     */
    void updateCache();

    /**
     * 检查当天是否可以日结（必须有订单）
     * 
     * @param dateStr 日期字符串（yyyy-MM-dd）
     * @return true-可以日结（有订单），false-不可以日结（空日结）
     */
    boolean canSettleToday(String dateStr);
}