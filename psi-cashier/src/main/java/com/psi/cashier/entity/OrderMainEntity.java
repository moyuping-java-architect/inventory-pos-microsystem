package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售订单主表实体类
 */
@Data
@TableName("order_main")
public class OrderMainEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 本地主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 订单号
     */
    private String orderNo;

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
     * 业务类型：20-零售 21-批发
     */
    private Integer bizType;

    /**
     * 应收总金额（含税）
     */
    private BigDecimal totalAmount;

    /**
     * 实收金额（含税）
     */
    private BigDecimal realAmount;

    /**
     * 整单优惠
     */
    private BigDecimal discountAmount;

    /**
     * 不含税商品净额
     */
    private BigDecimal netAmount;

    /**
     * 税额（VAT）
     */
    private BigDecimal taxAmount;

    /**
     * 结算币种（ZMW/USD）
     */
    private String currency;

    /**
     * 汇率（本位币 ZMW 对结算币种）
     */
    private BigDecimal exchangeRate;

    /**
     * 原币种应收金额
     */
    private BigDecimal originalAmount;

    /**
     * 会员ID
     */
    private Integer memberId;

    /**
     * 收银员ID
     */
    private Integer operatorId;

    /**
     * 支付状态：0-未结 1-已结
     */
    private Integer payStatus;

    /**
     * 日结状态：0-未日结 1-已日结
     */
    private Integer settleStatus;

    /**
     * 制单人
     */
    private String createBy;

    /**
     * 开单时间
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