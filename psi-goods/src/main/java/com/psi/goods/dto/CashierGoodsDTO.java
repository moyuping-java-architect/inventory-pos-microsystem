package com.psi.goods.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收银端商品展示DTO（按goods_unify_code聚合）
 */
@Data
public class CashierGoodsDTO {

    /**
     * 商品统一编码
     */
    private String goodsUnifyCode;

    /**
     * 商品名称（取第一个SKU的名称）
     */
    private String goodsName;

    /**
     * 最高销售价（同编码下最高价格）
     */
    private BigDecimal maxSalePrice;

    /**
     * 最低销售价（同编码下最低价格）
     */
    private BigDecimal minSalePrice;

    /**
     * 平均成本价
     */
    private BigDecimal avgCostPrice;

    /**
     * 总库存数量（同编码下所有SKU库存之和）
     */
    private Integer totalStockQty;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 基础单位
     */
    private String baseUnit;

    /**
     * 销售单位
     */
    private String saleUnit;

    /**
     * 单位换算
     */
    private String unitConversion;

    /**
     * 主图URL
     */
    private String imageUrl;

    /**
     * 规格描述
     */
    private String specDesc;

    /**
     * VAT税率
     */
    private BigDecimal taxRate;

    /**
     * 标价是否含税
     */
    private Integer isTaxInclusive;

    /**
     * USD销售价
     */
    private BigDecimal salePriceUsd;
}