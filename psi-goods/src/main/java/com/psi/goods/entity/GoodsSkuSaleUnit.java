package com.psi.goods.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * SKU销售单位实体
 * 支持非洲场景下多种销售单位销售模式
 * 例如：大米可以按袋(25kg)、按公斤、按小包(1kg)等多种单位销售
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_sku_sale_unit")
public class GoodsSkuSaleUnit extends BaseEntity {

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SKU编码（冗余字段）
     */
    private String skuCode;

    /**
     * 商品统一编码（同品多批次共用同一个编码，用于分组查询最高销售价）
     */
    private String goodsUnifyCode;

    /**
     * 条码（冗余字段，方便扫码查询，扫条码也能查出最高价）
     */
    private String barcode;

    /**
     * 商品名称（冗余字段，避免关联查询）
     */
    private String goodsName;

    /**
     * 商品分类ID（冗余字段，避免关联查询）
     */
    private Long categoryId;

    /**
     * 商品品牌ID（冗余字段，避免关联查询）
     */
    private Long brandId;

    /**
     * 商品图片URL（冗余字段，避免关联查询）
     */
    private String imageUrl;

    /**
     * 销售单位ID（关联goods_unit表）
     */
    private Long saleUnitId;

    /**
     * 销售单位名称
     */
    private String saleUnitName;

    /**
     * 销售单位符号
     */
    private String saleUnitSymbol;

    /**
     * 换算比例（相对于SKU基础单位的换算系数）
     * 例如：SKU基础单位是kg，销售单位是袋，每袋25kg，则换算比例为25
     */
    private BigDecimal conversionRate;

    /**
     * 包装规格描述（如：25kg/袋、1kg/小包）
     */
    private String packageSpec;

    /**
     * 销售价格（对应此销售单位的价格）
     */
    private BigDecimal salePrice;

    /**
     * 成本价格（对应此销售单位的成本）
     */
    private BigDecimal costPrice;

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
     * 是否管理批次/效期(0:否 1:是)
     */
    private Integer batchManaged;

    /**
     * 库存数量（对应此销售单位的库存）
     */
    private BigDecimal stockQty;

    /**
     * 最低库存
     */
    private BigDecimal minStockQty;

    /**
     * 是否默认销售单位（1-是，0-否）
     */
    private Integer isDefault;

    /**
     * 是否启用（1-启用，0-禁用）
     */
    private Integer status;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}