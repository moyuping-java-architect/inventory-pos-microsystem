package com.trademaster.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trademaster.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private String role;
}
