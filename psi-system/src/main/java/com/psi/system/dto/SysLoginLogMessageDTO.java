package com.psi.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录日志消息DTO
 * 
 * <p>用于MQ消息传递的登录日志数据结构
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysLoginLogMessageDTO {

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录类型（如：PASSWORD、TOKEN等）
     */
    private String loginType;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;

    /**
     * 是否成功（1:成功, 0:失败）
     */
    private Integer success;

    /**
     * 错误信息
     */
    private String errorMessage;
}