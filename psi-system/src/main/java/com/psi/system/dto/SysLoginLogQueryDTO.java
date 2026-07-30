package com.psi.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志查询DTO
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class SysLoginLogQueryDTO {

    private Long userId;

    private String username;

    private String loginType;

    private String ipAddress;

    private Integer success;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}