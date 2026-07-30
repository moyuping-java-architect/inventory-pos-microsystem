package com.trademaster.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("wf_operation_log")
public class WfOperationLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dataUuid;

    private Long tenantId;

    private String processInstanceId;

    private String operatorId;

    private String operatorName;

    private Integer operateType;

    private String operateContent;

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
