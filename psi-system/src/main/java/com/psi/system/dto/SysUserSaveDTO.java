package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserSaveDTO implements Serializable {

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Long deptId;
}