package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserDTO implements Serializable {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Long deptId;

    private String deptName;

    private String shopName;

    private Integer status;

    private Long tenantId;

    private Long shopId;

    private Long warehouseId;

    private Long roleId;

    private String roleName;

    private String permissions;
}