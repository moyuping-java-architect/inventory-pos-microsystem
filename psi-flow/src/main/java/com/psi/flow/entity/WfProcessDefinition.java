package com.psi.flow.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流-流程定义实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_definition")
public class WfProcessDefinition extends BaseEntity {

    /**
     * 流程唯一标识
     */
    private String processKey;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 备注
     */
    private String remark;
}