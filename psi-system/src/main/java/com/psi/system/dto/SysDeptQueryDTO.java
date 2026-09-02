package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysDeptQueryDTO implements Serializable {

    private String deptName;

    private String deptCode;

    private Long parentId;

    private Long shopId;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}