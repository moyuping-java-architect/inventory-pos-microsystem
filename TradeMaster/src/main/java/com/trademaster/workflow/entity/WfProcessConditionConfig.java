package com.trademaster.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("wf_process_condition_config")
public class WfProcessConditionConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dataUuid;

    private Long tenantId;

    private Long processDefId;

    private String conditionName;

    private String conditionKey;

    private String conditionType;

    private String compareType;

    private String defaultValue;

    private Integer sort;

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
