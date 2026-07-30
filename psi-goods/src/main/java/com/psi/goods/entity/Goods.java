package com.psi.goods.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品主表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods")
public class Goods extends BaseEntity {

    /**
     * 商品编码
     */
    private String goodsCode;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品英文名称
     */
    private String goodsNameEn;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 计量单位
     */
    private String unit;

    /**
     * 主图URL
     */
    private String imageUrl;

    /**
     * 图片列表（JSON数组）
     */
    private String images;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品特性（JSON数组）
     */
    private String features;

    /**
     * 库存数量
     */
    private Integer stockQty;

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 重量
     */
    private Integer weight;

    /**
     * 重量单位
     */
    private String weightUnit;

    /**
     * 包装规格
     */
    private String packageSpec;

    /**
     * 认证信息
     */
    private String certification;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}