package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysTenantDTO implements Serializable {

    private Long id;

    private String tenantName;

    private String tenantCode;

    private String contactName;

    private String contactPhone;

    private String email;

    private String address;

    private LocalDateTime expireTime;

    private Integer status;
}