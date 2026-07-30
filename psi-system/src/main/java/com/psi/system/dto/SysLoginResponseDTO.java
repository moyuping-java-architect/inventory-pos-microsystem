package com.psi.system.dto;

import lombok.Data;

@Data
public class SysLoginResponseDTO {

    private String token;

    private String refreshToken;

    private Long expiresIn;

    private SysUserDTO userInfo;
}