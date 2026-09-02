package com.psi.goods.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsQueryDTO extends PageQueryDTO {

    /**
     * 商品编码（模糊匹配）
     */
    private String goodsCode;

    /**
     * 商品名称（模糊匹配）
     */
    private String goodsName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 是否有库存
     */
    private Boolean hasStock;

    /**
     * 状态
     */
    private Integer status;
}