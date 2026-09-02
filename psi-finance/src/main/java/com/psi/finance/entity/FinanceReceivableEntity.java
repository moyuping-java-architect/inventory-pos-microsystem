package com.psi.finance.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_receivable")
public class FinanceReceivableEntity extends BaseEntity {

    private String storeCode;
    private String storeName;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainAmount;
    private String sourceNo;
    private String sourceType;
    private String billDate;
    private String dueDate;
}