package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysTenantQueryDTO implements Serializable {

    private String tenantName;

    private String tenantCode;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}