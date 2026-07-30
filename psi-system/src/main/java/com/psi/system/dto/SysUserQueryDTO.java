package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserQueryDTO implements Serializable {

    private String username;

    private String nickname;

    private Long deptId;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}