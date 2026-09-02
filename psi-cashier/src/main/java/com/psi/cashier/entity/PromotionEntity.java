package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 促销活动实体（收银端本地副本）
 * 通过 psi-sync 从 psi-sale 服务端同步
 */
@Data
@TableName("promotion")
public class PromotionEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String dataUuid;

    private String tenantId;

    @TableField("promotion_no")
    private String promotionNo;

    @TableField("promotion_name")
    private String promotionName;

    @TableField("promotion_type")
    private Integer promotionType;

    @TableField("discount_type")
    private Integer discountType;

    @TableField("discount_value")
    private BigDecimal discountValue;

    @TableField("min_amount")
    private BigDecimal minAmount;

    @TableField("min_quantity")
    private BigDecimal minQuantity;

    @TableField("start_time")
    private String startTime;

    @TableField("end_time")
    private String endTime;

    @TableField("scope_type")
    private Integer scopeType;

    private Integer status;

    private Integer priority;

    private Integer superimposable;

    private String remark;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
