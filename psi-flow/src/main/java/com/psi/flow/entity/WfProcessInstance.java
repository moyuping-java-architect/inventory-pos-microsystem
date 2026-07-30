package com.psi.flow.entity;

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

    private Long createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

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
}