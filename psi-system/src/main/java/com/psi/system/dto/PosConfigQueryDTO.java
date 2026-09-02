package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PosConfigQueryDTO implements Serializable {

    private String posSn;

    private String shopCode;

    private String posId;

    private String posName;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}