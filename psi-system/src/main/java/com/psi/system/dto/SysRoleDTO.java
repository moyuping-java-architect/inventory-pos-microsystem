package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysRoleDTO implements Serializable {

    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer sortOrder;

    private Integer status;
}