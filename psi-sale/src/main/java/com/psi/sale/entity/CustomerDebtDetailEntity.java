package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_debt_detail")
public class CustomerDebtDetailEntity extends BaseEntity {

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
     * 关联单据类型：1-销售订单 2-销售出库
     */
    private Integer billType;

    /**
     * 关联单据编号
     */
    private String billNo;

    /**
     * 欠款金额
     */
    private BigDecimal debtAmount;

    /**
     * 已还金额
     */
    private BigDecimal paidAmount;

    /**
     * 待还金额
     */
    private BigDecimal pendingAmount;

    /**
     * 欠款日期
     */
    private String debtDate;

    /**
     * 到期日期
     */
    private String dueDate;

    /**
     * 状态：1-待还款 2-部分还款 3-已结清
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}