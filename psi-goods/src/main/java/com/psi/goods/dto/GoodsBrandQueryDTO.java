package com.psi.goods.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品品牌查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsBrandQueryDTO extends PageQueryDTO {

    /**
     * 品牌编码（模糊匹配）
     */
    private String brandCode;

    /**
     * 品牌名称（模糊匹配）
     */
    private String brandName;

    /**
     * 状态
     */
    private Integer status;
}