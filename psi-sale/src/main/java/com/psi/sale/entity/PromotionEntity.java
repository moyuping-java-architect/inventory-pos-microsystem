package com.psi.sale.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion")
public class PromotionEntity extends BaseEntity {

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
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("scope_type")
    private Integer scopeType;

    @TableField("status")
    private Integer status;

    @TableField("priority")
    private Integer priority;

    @TableField("superimposable")
    private Integer superimposable;

    @TableField("remark")
    private String remark;
}
