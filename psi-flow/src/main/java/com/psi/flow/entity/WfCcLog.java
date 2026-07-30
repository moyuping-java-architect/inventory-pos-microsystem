package com.psi.flow.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 抄送记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_cc_log")
public class WfCcLog extends BaseEntity {

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 抄送用户ID
     */
    private String ccUserId;

    /**
     * 抄送用户姓名
     */
    private String ccUserName;

    /**
     * 状态：1-未读 2-已读（覆盖父类status字段）
     */
    private Integer status;
}