package com.psi.system.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    private String deptName;

    private Long parentId;

    private String deptCode;

    private String leader;

    private String phone;

    private Integer sortOrder;

    private Long shopId;

    private String description;
}