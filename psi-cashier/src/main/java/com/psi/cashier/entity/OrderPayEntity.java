package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售支付记录表实体类
 */
@Data
@TableName("order_pay")
public class OrderPayEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 门店编码
     */
    private String shopCode;

    /**
     * 收银机编号
     */
    private String posId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 业务类型：60-零售收款 61-批发收款
     */
    private Integer bizType;

    /**
     * 支付方式ID
     */
    private Integer payId;

    /**
     * 支付类型：0-普通 1-Mobile Money 2-银行卡
     */
    private Integer payType;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付币种
     */
    private String currency;

    /**
     * 支付时间
     */
    private String payTime;

    /**
     * Mobile Money 运营商
     */
    private String mobileProvider;

    /**
     * Mobile Money 手机号
     */
    private String mobilePhone;

    /**
     * Mobile Money 交易流水号
     */
    private String mobileTransactionNo;

    /**
     * 操作人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private String updateTime;
}