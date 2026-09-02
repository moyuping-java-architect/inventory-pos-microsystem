package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysDictTypeQueryDTO implements Serializable {

    private String dictCode;

    private String dictName;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}