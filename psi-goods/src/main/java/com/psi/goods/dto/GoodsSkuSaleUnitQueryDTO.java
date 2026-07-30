package com.psi.goods.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SKU销售单位查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsSkuSaleUnitQueryDTO extends PageQueryDTO {

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品统一编码
     */
    private String goodsUnifyCode;

    /**
     * 销售单位ID
     */
    private Long saleUnitId;

    /**
     * 销售单位名称（模糊匹配）
     */
    private String saleUnitName;

    /**
     * 销售单位符号
     */
    private String saleUnitSymbol;

    /**
     * 是否默认销售单位
     */
    private Integer isDefault;

    /**
     * 是否启用
     */
    private Integer status;
}