package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PosOperatorSaveDTO implements Serializable {

    private String shopCode;

    private String username;

    private String password;

    private String realName;

    private Integer role;

    private Integer status;
}