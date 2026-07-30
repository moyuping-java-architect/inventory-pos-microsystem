package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysDictDataDTO implements Serializable {

    private Long id;

    private String dictCode;

    private String dictValue;

    private String dictLabel;

    private Integer sortOrder;

    private Integer status;
}