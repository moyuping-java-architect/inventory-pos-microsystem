package com.trademaster.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("wf_process_relation")
public class WfProcessRelation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dataUuid;

    private Long tenantId;

    private Long processDefId;

    private Long fromNodeId;

    private Long toNodeId;

    private String conditionExpr;

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
