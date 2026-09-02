package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 销冠话术库（旅程触达话术）
 * 核心：客户在某旅程节点停留第 N 天 + 命中标签 + 未发送过 -> 触达该话术
 */
@Data
@TableName("sales_script_library")
public class SalesScriptLibraryEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 话术编码（含版本，如：CONSIDERING_D1_V1） */
    @TableField("script_code")
    private String scriptCode;

    /** 话术名称 */
    @TableField("script_name")
    private String scriptName;

    /** 分类：ICE_BREAK/SOLUTION/CLOSE/REACTIVATE/SALES */
    @TableField("category")
    private String category;

    /** 话术正文（支持占位符如 {{name}}/{{school}}/{{product}}） */
    @TableField("content")
    private String content;

    /** 适用渠道 */
    @TableField("channel")
    private String channel;

    /** 匹配客户旅程阶段编码 */
    @TableField("stage_code")
    private String stageCode;

    /** 阶段名称（展示用） */
    @TableField("stage_name")
    private String stageName;

    /** 匹配标签编码集合，逗号分隔 */
    @TableField("tags")
    private String tags;

    /** 标签匹配模式：ANY 任一命中 / ALL 全部命中 */
    @TableField("tag_match_mode")
    private String tagMatchMode;

    /** 阶段停留天数开始（含） */
    @TableField("day_start")
    private Integer dayStart;

    /** 阶段停留天数结束（含） */
    @TableField("day_end")
    private Integer dayEnd;

    /** 优先级，数值越大越优先 */
    @TableField("priority")
    private Integer priority;

    /** 同一客户在该节点-天数区间的最大发送次数 */
    @TableField("send_limit")
    private Integer sendLimit;

    /** 启用状态 0-禁用 1-启用 */
    @TableField("is_enabled")
    private Integer isEnabled;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
