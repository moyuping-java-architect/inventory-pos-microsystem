package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_debt")
public class CustomerDebtEntity extends BaseEntity {

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 累计欠款金额
     */
    private BigDecimal totalDebtAmount;

    /**
     * 已还款金额
     */
    private BigDecimal paidAmount;

    /**
     * 待还款金额（欠款余额）
     */
    private BigDecimal pendingAmount;

    /**
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 可用信用额度
     */
    private BigDecimal availableCredit;

    /**
     * 备注
     */
    private String remark;
}