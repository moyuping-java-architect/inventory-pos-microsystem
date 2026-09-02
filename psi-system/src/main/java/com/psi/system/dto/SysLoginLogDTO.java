package com.psi.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志DTO
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class SysLoginLogDTO {

    private Long id;

    private Long tenantId;

    private Long userId;

    private String username;

    private String loginType;

    private LocalDateTime loginTime;

    private String ipAddress;

    private String userAgent;

    private Integer success;

    private String errorMessage;

    private LocalDateTime createTime;
}