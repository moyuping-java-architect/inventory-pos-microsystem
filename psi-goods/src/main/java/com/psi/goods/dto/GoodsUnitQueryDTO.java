package com.psi.goods.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品单位查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsUnitQueryDTO extends PageQueryDTO {

    /**
     * 单位编码（模糊匹配）
     */
    private String unitCode;

    /**
     * 单位名称（模糊匹配）
     */
    private String unitName;

    /**
     * 单位符号（模糊匹配）
     */
    private String unitSymbol;

    /**
     * 单位类型
     */
    private String unitType;

    /**
     * 状态
     */
    private Integer status;
}