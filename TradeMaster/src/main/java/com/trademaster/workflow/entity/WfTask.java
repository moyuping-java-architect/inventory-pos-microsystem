package com.trademaster.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("wf_task")
public class WfTask implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    private Long tenantId;

    private String processInstanceId;

    private Long nodeId;

    private String taskName;

    private String handlerUserId;

    private String handlerUserName;

    private Integer status;

    private String handleNote;

    private LocalDateTime handleTime;

    @TableLogic
    private Integer delFlag;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
