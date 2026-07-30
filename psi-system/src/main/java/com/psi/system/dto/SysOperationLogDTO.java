package com.psi.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志DTO
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class SysOperationLogDTO {

    private Long id;

    private Long tenantId;

    private Long userId;

    private String username;

    private String operationType;

    private String moduleName;

    private String operationDesc;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String responseData;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime operationTime;

    private Long executionTime;

    private Integer success;

    private String errorMessage;

    private LocalDateTime createTime;
}