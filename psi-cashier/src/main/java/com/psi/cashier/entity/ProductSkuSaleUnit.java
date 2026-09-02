package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU多销售单位实体
 * 支持非洲场景下多种销售单位销售模式
 */
@Data
@TableName("product_sku_sale_unit")
public class ProductSkuSaleUnit {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 数据版本号（用于冲突解决）
     */
    private Long dataVersion;

    private Long tenantId;

    private Long skuId;

    private String skuNo;

    private String barcode;

    private String goodsName;

    private Long categoryId;

    private Long brandId;

    private Long saleUnitId;

    private String saleUnitName;

    private String saleUnitSymbol;

    private BigDecimal conversionRate;

    private String packageSpec;

    private BigDecimal salePrice;

    private BigDecimal taxRate;

    private Integer isTaxInclusive;

    private BigDecimal salePriceUsd;

    private Integer batchManaged;

    private Integer isDefault;

    private Integer status;

    private Integer sortOrder;

    private Integer delFlag;

    private Long createBy;

    private String createTime;

    private Long updateBy;

    private String updateTime;
}