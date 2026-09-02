package com.psi.flow.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_operation_log")
public class WfOperationLog extends BaseEntity {

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作类型：1-发起 2-同意 3-驳回 4-撤销
     */
    private Integer operateType;

    /**
     * 操作内容
     */
    private String operateContent;
}