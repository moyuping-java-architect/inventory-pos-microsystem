package com.psi.flow.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程条件配置实体
 * 核心：不同流程配置不同条件字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_condition_config")
public class WfProcessConditionConfig extends BaseEntity {

    /**
     * 关联流程定义ID
     */
    private Long processDefId;

    /**
     * 条件名称
     */
    private String conditionName;

    /**
     * 变量key
     */
    private String conditionKey;

    /**
     * 类型：number/string/boolean
     */
    private String conditionType;

    /**
     * 运算符：> < >= <= =
     */
    private String compareType;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 排序
     */
    private Integer sort;
}