package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysMenuSaveDTO implements Serializable {

    private Long id;

    private String menuName;

    private Long parentId;

    private String path;

    private String component;

    private String permissionCode;

    private String icon;

    private Integer menuType;

    private Integer sortOrder;

    private Integer isHidden;
}