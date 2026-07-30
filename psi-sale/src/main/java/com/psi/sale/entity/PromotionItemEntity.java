package com.psi.sale.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion_item")
public class PromotionItemEntity extends BaseEntity {

    @TableField("promotion_id")
    private Long promotionId;

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
}
