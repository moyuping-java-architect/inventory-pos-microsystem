package com.psi.flow.entity;

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

    private Long createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    private String processInstanceId;

    private Long nodeId;

    private String taskName;

    private String handlerUserId;

    private String handlerUserName;

    private Integer status;

    private String handleNote;

    private LocalDateTime handleTime;
}