package com.psi.finance.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_account")
public class FinanceAccountEntity extends BaseEntity {

    private String storeCode;
    private String storeName;
    private String accountType;
    private String accountName;
    private String accountNo;
    private BigDecimal balance;
    private String remark;
}