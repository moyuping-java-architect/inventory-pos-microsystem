package com.psi.system.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
public class SysRoleMenu extends BaseEntity {

    private Long roleId;

    private Long menuId;
}