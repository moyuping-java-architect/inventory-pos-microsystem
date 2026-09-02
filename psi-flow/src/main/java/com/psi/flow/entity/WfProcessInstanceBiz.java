package com.psi.flow.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程业务关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_instance_biz")
public class WfProcessInstanceBiz extends BaseEntity {

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID
     */
    private String bizId;
}