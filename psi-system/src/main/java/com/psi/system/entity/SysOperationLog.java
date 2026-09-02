package com.psi.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("operation_type")
    private String operationType;

    @TableField("module_name")
    private String moduleName;

    @TableField("operation_desc")
    private String operationDesc;

    @TableField("request_url")
    private String requestUrl;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_params")
    private String requestParams;

    @TableField("response_data")
    private String responseData;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("operation_time")
    private LocalDateTime operationTime;

    @TableField("execution_time")
    private Long executionTime;

    @TableField("success")
    private Integer success;

    @TableField("error_message")
    private String errorMessage;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    private Long updateBy;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField("del_flag")
    @TableLogic
    private Integer delFlag;

    @TableField("status")
    private Integer status;
}