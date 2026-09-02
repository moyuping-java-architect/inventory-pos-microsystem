package com.psi.goods.dto;

import lombok.Data;

/**
 * 收银端商品查询DTO
 */
@Data
public class CashierGoodsQueryDTO {

    /**
     * 商品统一编码（模糊匹配）
     */
    private String goodsUnifyCode;

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
     * 是否有库存（true-只查有库存的）
     */
    private Boolean hasStock;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;
}