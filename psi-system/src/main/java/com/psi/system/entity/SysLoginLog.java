package com.psi.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("login_type")
    private String loginType;

    @TableField("login_time")
    private LocalDateTime loginTime;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

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