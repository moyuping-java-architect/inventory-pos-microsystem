package com.psi.finance.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_daily_ledger")
public class FinanceDailyLedgerEntity extends BaseEntity {

    private String storeCode;
    private String storeName;
    private String ledgerDate;
    private BigDecimal saleAmount;
    private BigDecimal costAmount;
    private BigDecimal profitAmount;
    private BigDecimal cashIn;
    private BigDecimal cashOut;
    private BigDecimal transferIn;
    private BigDecimal transferOut;
    private BigDecimal receivableAmount;
    private BigDecimal payableAmount;
    private BigDecimal beginningBalance;
    private BigDecimal endingBalance;
}