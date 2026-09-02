package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Customer journey zero-code configuration entity.
 * Each row is one configurable parameter (churn threshold, message template, etc.)
 * Grouped by config_group for the admin form rendering.
 */
@Data
@TableName("customer_journey_config")
public class CustomerJourneyConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** CHURN_MODEL / MESSAGE_TEMPLATE / TOUCHPOINT_TYPE / CHANNEL_RULE / SUGGESTED_ACTION / DASHBOARD */
    private String configGroup;

    /** Unique key within the group, e.g. "active_multiplier" */
    private String configKey;

    /** Value as string (numbers stored as text, parsed by value_type in service layer) */
    private String configValue;

    /** NUMBER / STRING / BOOLEAN / JSON / OPTION — tells the frontend how to render */
    private String valueType;

    /** Human-readable label for the admin form */
    private String displayName;

    /** Help text shown below the field */
    private String description;

    /** Sort order within group */
    private Integer sortOrder;

    @TableLogic
    private Integer delFlag;

    private String createTime;
    private String updateTime;
}
