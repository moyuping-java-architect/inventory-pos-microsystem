package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员实体类
 */
@Data
@TableName("member")
public class MemberEntity {

    @TableId(value = "member_id", type = IdType.AUTO)
    private Integer memberId;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("data_uuid")
    private String dataUuid;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("phone")
    private String phone;

    @TableField("name")
    private String name;

    @TableField("password")
    private String password;

    @TableField("balance")
    private Double balance;

    @TableField("point")
    private Integer point;

    @TableField("level")
    private Integer level;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}