package com.psi.flow.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流-节点流转关系实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_relation")
public class WfProcessRelation extends BaseEntity {

    /**
     * 流程定义ID
     */
    private Long processDefId;

    /**
     * 来源节点ID
     */
    private Long fromNodeId;

    /**
     * 目标节点ID
     */
    private Long toNodeId;

    /**
     * EL条件表达式
     */
    private String conditionExpr;
}