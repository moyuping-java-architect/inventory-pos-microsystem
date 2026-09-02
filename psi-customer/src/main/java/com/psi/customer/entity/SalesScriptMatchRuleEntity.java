package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 话术匹配规则（权重配置）
 */
@Data
@TableName("sales_script_match_rule")
public class SalesScriptMatchRuleEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 规则名称 */
    @TableField("rule_name")
    private String ruleName;

    /** 节点匹配是否启用 */
    @TableField("stage_enabled")
    private Integer stageEnabled;

    /** 节点权重 */
    @TableField("stage_weight")
    private BigDecimal stageWeight;

    /** 标签重叠是否启用 */
    @TableField("tag_enabled")
    private Integer tagEnabled;

    /** 标签权重 */
    @TableField("tag_weight")
    private BigDecimal tagWeight;

    /** 阶段停留时间是否启用 */
    @TableField("time_enabled")
    private Integer timeEnabled;

    /** 时间权重 */
    @TableField("time_weight")
    private BigDecimal timeWeight;

    /** 优先级是否启用 */
    @TableField("priority_enabled")
    private Integer priorityEnabled;

    /** 优先级权重 */
    @TableField("priority_weight")
    private BigDecimal priorityWeight;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
