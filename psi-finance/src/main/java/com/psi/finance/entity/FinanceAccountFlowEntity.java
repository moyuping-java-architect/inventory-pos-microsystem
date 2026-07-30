package com.psi.finance.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_account_flow")
public class FinanceAccountFlowEntity extends BaseEntity {

    private String storeCode;
    private String storeName;
    private String accountType;
    private String accountName;
    private Integer flowType;
    private BigDecimal inAmount;
    private BigDecimal outAmount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String sourceNo;
    private String sourceType;
    private String payNo;
    private String remark;
}