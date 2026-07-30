package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PosOperatorDTO implements Serializable {

    private Long id;

    private String dataUuid;

    private Long tenantId;

    private String shopCode;

    private String username;

    private String password;

    private String realName;

    private Integer role;

    private Integer status;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;
}