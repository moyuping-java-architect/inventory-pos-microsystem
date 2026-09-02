package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysDictTypeDTO implements Serializable {

    private Long id;

    private String dictCode;

    private String dictName;

    private String description;

    private Integer status;
}