package com.psi.goods.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收银端销售单位展示DTO
 * 前端收银查找商品时使用此DTO展示各销售单位的价格和库存
 */
@Data
public class CashierSaleUnitDTO {

    /**
     * 商品统一编码
     */
    private String goodsUnifyCode;

    /**
     * 商品ID（关联goods表）
     */
    private Long goodsId;

    /**
     * 商品编码（goods.goods_code）
     */
    private String goodsCode;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 条码
     */
    private String barcode;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 销售单位ID
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
     * 销售价格（最高价格）
     */
    private BigDecimal salePrice;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    private BigDecimal stockQty;

    /**
     * 换算比例（相对于基础单位）
     */
    private BigDecimal conversionRate;

    /**
     * 包装规格
     */
    private String packageSpec;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 主图URL
     */
    private String imageUrl;

    /**
     * 是否默认销售单位
     */
    private Integer isDefault;
}