package com.psi.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志查询DTO
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class SysOperationLogQueryDTO {

    private Long userId;

    private String username;

    private String operationType;

    private String moduleName;

    private String ipAddress;

    private Integer success;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}