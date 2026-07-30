package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_payment")
public class CustomerPaymentEntity extends BaseEntity {

    /**
     * 还款单编号
     */
    private String paymentNo;

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
     * 还款日期
     */
    private String paymentDate;

    /**
     * 还款金额
     */
    private BigDecimal paymentAmount;

    /**
     * 还款方式：1-现金 2-银行转账 3-支付宝 4-微信 5-其他
     */
    private Integer paymentMethod;

    /**
     * 收款账户
     */
    private String accountNo;

    /**
     * 收款银行
     */
    private String bankName;

    /**
     * 备注
     */
    private String remark;
}