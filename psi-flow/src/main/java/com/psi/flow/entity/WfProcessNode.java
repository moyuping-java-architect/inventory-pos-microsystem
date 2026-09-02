package com.psi.flow.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流-流程节点实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_node")
public class WfProcessNode extends BaseEntity {

    /**
     * 流程定义ID
     */
    private Long processDefId;

    /**
     * 节点标识
     */
    private String nodeKey;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型：1-审批 2-条件 3-抄送 4-结束
     */
    private Integer nodeType;

    /**
     * 审批类型：1-单人 2-会签 3-或签
     */
    private Integer approveType;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 节点自定义配置（JSON格式）
     */
    private String config;
}