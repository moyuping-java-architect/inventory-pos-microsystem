package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员等级实体类
 */
@Data
@TableName("member_level")
public class MemberLevelEntity {

    @TableId(value = "level_id", type = IdType.INPUT)
    private Integer levelId;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("data_uuid")
    private String dataUuid;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("level_name")
    private String levelName;

    @TableField("discount")
    private Double discount;

    @TableField("need_point")
    private Integer needPoint;
}