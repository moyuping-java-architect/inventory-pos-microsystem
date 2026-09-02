package com.psi.goods.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_category")
public class GoodsCategory extends BaseEntity {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类层级
     */
    private Integer level;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 分类描述
     */
    private String description;
}