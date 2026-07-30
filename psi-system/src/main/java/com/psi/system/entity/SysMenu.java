package com.psi.system.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

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