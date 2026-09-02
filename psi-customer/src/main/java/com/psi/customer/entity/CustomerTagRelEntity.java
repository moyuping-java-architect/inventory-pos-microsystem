package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客户标签关系
 */
@Data
@TableName("customer_tag_rel")
public class CustomerTagRelEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 客户ID */
    @TableField("customer_id")
    private Long customerId;

    /** 标签编码 */
    @TableField("tag_code")
    private String tagCode;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
