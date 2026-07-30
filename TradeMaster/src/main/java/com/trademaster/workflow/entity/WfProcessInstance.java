package com.trademaster.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("wf_process_instance")
public class WfProcessInstance implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    private Long tenantId;

    private Long processDefId;

    private String processKey;

    private String title;

    private String startUserId;

    private String startUserName;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long currentNodeId;

    private Integer status;

    @TableField(value = "ext_json", exist = true, updateStrategy = FieldStrategy.ALWAYS)
    private String extJson;

    private LocalDateTime endTime;

    @TableLogic
    private Integer delFlag;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
