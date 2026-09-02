package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员价实体类
 */
@Data
@TableName("member_price")
public class MemberPriceEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("data_uuid")
    private String dataUuid;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("sku_id")
    private Integer skuId;

    @TableField("member_level")
    private Integer memberLevel;

    @TableField("member_price")
    private Double memberPrice;

    @TableField("start_time")
    private String startTime;

    @TableField("end_time")
    private String endTime;

    @TableField("status")
    private Integer status;
}