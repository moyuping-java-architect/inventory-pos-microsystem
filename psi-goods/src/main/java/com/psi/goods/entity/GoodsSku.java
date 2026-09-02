package com.psi.goods.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品SKU实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_sku")
public class GoodsSku extends BaseEntity {

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 规格值（逗号分隔）
     */
    private String specValues;

    /**
     * 规格JSON
     */
    private String specJson;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 销售价
     */
    private BigDecimal salePrice;

    /**
     * 市场价
     */
    private BigDecimal marketPrice;

    /**
     * 条形码
     */
    private String barcode;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 库存数量
     */
    private Integer stockQty;

    /**
     * 最低库存
     */
    private Integer minStockQty;

    /**
     * 最高库存
     */
    private Integer maxStockQty;

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 重量
     */
    private BigDecimal weight;

    /**
     * 重量单位
     */
    private String weightUnit;

    /**
     * 体积
     */
    private BigDecimal volume;

    /**
     * 体积单位
     */
    private String volumeUnit;

    /**
     * 包装规格
     */
    private String packageSpec;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * 基础单位（如：kg、g、件）
     */
    private String baseUnit;

    /**
     * 销售单位（如：包、箱、盒）
     */
    private String saleUnit;

    /**
     * 单位换算（如：0.4kg/包）
     */
    private String unitConversion;

    /**
     * 商品统一编码（同品多批次共用同一个编码）
     */
    private String goodsUnifyCode;

    /**
     * 商品编码
     */
    private String goodsCode;

    /**
     * VAT税率（如0.1600表示16%）
     */
    private BigDecimal taxRate;

    /**
     * 标价是否含税(0:否 1:是)
     */
    private Integer isTaxInclusive;

    /**
     * USD销售价
     */
    private BigDecimal salePriceUsd;

    /**
     * USD成本价
     */
    private BigDecimal costPriceUsd;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}