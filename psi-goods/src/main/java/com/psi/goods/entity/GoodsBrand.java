package com.psi.goods.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品品牌实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_brand")
public class GoodsBrand extends BaseEntity {

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 品牌编码
     */
    private String brandCode;

    /**
     * 品牌Logo
     */
    private String brandLogo;

    /**
     * 品牌描述
     */
    private String brandDesc;

    /**
     * 品牌官网
     */
    private String website;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}