package com.psi.goods.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品SKU查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsSkuQueryDTO extends PageQueryDTO {

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * SKU编码（模糊匹配）
     */
    private String skuCode;

    /**
     * 统一编码
     */
    private String goodsUnifyCode;

    /**
     * 条码
     */
    private String barcode;

    /**
     * 是否有库存
     */
    private Boolean hasStock;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 关键词（同时模糊匹配SKU编码、统一编码、条码）
     */
    private String keyword;
}