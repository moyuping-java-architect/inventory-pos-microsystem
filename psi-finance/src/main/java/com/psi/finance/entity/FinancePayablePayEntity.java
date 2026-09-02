package com.psi.finance.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_payable_pay")
public class FinancePayablePayEntity extends BaseEntity {

    private String storeCode;
    private String storeName;
    private Long payableId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal payAmount;
    private String payMethod;
    private String payNo;
    private String payDate;
    private String remark;
}