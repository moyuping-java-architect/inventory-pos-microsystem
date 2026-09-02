package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysDictDataQueryDTO implements Serializable {

    private String dictCode;

    private String dictValue;

    private String dictLabel;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}