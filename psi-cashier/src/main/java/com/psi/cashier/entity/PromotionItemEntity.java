package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 促销活动商品范围实体
 */
@Data
@TableName("promotion_item")
public class PromotionItemEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String dataUuid;

    private String tenantId;

    @TableField("promotion_id")
    private Integer promotionId;

    @TableField("promotion_no")
    private String promotionNo;

    @TableField("item_type")
    private Integer itemType;

    @TableField("item_code")
    private String itemCode;

    @TableField("item_name")
    private String itemName;

    @TableField("category_code")
    private String categoryCode;

    @TableField("category_name")
    private String categoryName;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
