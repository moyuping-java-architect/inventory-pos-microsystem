package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysDeptSaveDTO implements Serializable {

    private Long id;

    private String deptName;

    private Long parentId;

    private String deptCode;

    private String leader;

    private String phone;

    private Integer sortOrder;

    private Long shopId;

    private String description;
}