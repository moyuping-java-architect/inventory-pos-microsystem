package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_pending")
public class OrderPendingEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 挂单号
     */
    private String pendingNo;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 店铺编码
     */
    private String shopCode;

    /**
     * 收银机编号
     */
    private String posId;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 操作员ID
     */
    private Integer operatorId;

    /**
     * 挂单名称
     */
    private String pendingName;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private String updateTime;
}