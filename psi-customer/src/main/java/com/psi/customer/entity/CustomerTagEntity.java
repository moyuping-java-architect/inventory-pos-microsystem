package com.psi.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客户标签字典
 */
@Data
@TableName("customer_tag")
public class CustomerTagEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private String tenantId;

    /** 标签编码 */
    @TableField("tag_code")
    private String tagCode;

    /** 标签名称 */
    @TableField("tag_name")
    private String tagName;

    /** 分类：INDUSTRY/BUDGET/SOURCE/DEFAULT */
    @TableField("category")
    private String category;

    /** 标签颜色 */
    @TableField("color")
    private String color;

    /** 排序 */
    @TableField("sort")
    private Integer sort;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;

    @TableField("del_flag")
    private Integer delFlag;
}
