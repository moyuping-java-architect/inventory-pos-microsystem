package com.psi.goods.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsCategoryQueryDTO extends PageQueryDTO {

    /**
     * 分类编码（模糊匹配）
     */
    private String categoryCode;

    /**
     * 分类名称（模糊匹配）
     */
    private String categoryName;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 状态
     */
    private Integer status;
}