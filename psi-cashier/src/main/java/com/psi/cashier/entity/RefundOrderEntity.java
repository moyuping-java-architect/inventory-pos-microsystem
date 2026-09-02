package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退货单主表实体类
 * 存储退货订单的基本信息，与销售订单关联
 */
@Data
@TableName("refund_order")
public class RefundOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 退货ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 退货单号
     */
    private String refundNo;

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
     * 业务类型：3-退货
     */
    private Integer bizType;

    /**
     * 原销售订单号
     */
    private String sourceOrderNo;

    /**
     * 退货操作员
     */
    private Integer operatorId;

    /**
     * 退款总金额
     */
    private BigDecimal totalRefund;

    /**
     * 不含税退款净额
     */
    private BigDecimal netRefund;

    /**
     * 退款 VAT 税额
     */
    private BigDecimal taxRefund;

    /**
     * 退款币种
     */
    private String currency;

    /**
     * 汇率（本位币对退款币种）
     */
    private BigDecimal exchangeRate;

    /**
     * 原币种退款金额
     */
    private BigDecimal originalRefund;

    /**
     * 退货时间
     */
    private String refundTime;

    /**
     * 退货类型：1-整单退 2-单品退
     */
    private Integer refundType;

    /**
     * 退货原因
     */
    private String remark;

    /**
     * 制单人
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