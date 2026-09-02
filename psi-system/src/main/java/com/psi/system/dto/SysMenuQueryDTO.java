package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysMenuQueryDTO implements Serializable {

    private String menuName;

    private Long parentId;

    private Integer menuType;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}