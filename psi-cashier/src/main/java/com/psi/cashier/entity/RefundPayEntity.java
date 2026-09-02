package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款支付表实体类
 * 存储退货退款的支付方式和金额信息
 */
@Data
@TableName("refund_pay")
public class RefundPayEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 退款ID
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
     * 退货单号
     */
    private String refundNo;

    /**
     * 业务类型：7-退款
     */
    private Integer bizType;

    /**
     * 退款方式ID
     */
    private Integer payId;

    /**
     * 退款方式名称
     */
    private String payName;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款币种
     */
    private String currency;

    /**
     * 退款时间
     */
    private String refundTime;

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