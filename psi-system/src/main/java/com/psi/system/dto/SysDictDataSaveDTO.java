package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysDictDataSaveDTO implements Serializable {

    private Long id;

    private String dictCode;

    private String dictValue;

    private String dictLabel;

    private Integer sortOrder;
}