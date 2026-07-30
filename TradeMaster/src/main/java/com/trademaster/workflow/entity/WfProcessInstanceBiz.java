package com.trademaster.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("wf_process_instance_biz")
public class WfProcessInstanceBiz implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dataUuid;

    private Long tenantId;

    private String processInstanceId;

    private String bizType;

    private String bizId;

    private Integer status;

    @TableLogic
    private Integer delFlag;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
