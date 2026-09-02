package com.psi.finance.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_daily_close")
public class FinanceDailyCloseEntity extends BaseEntity {

    private String storeCode;
    private String storeName;
    private String closeDate;
    private BigDecimal saleAmount;
    private BigDecimal costAmount;
    private BigDecimal profitAmount;
    private BigDecimal cashIn;
    private BigDecimal cashOut;
    private BigDecimal transferIn;
    private BigDecimal transferOut;
    private BigDecimal receivableAmount;
    private BigDecimal payableAmount;
    private BigDecimal cashBalance;
    private BigDecimal wechatBalance;
    private BigDecimal alipayBalance;
    private BigDecimal bankBalance;
    private BigDecimal totalBalance;
    private String closeBy;
    private String closeTime;
    private Integer closeStatus;
    private String remark;
}