package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PosOperatorQueryDTO implements Serializable {

    private String shopCode;

    private String username;

    private String realName;

    private Integer role;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}