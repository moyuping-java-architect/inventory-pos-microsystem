package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysRoleQueryDTO implements Serializable {

    private String roleName;

    private String roleCode;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}