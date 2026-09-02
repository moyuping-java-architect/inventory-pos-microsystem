package com.psi.goods.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品单位实体
 * 用于管理商品的计量单位（如：kg、g、包、箱、盒等）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_unit")
public class GoodsUnit extends BaseEntity {

    /**
     * 单位编码
     */
    private String unitCode;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 单位符号（如：kg、m、件）
     */
    private String unitSymbol;

    /**
     * 单位类型（WEIGHT-重量单位，VOLUME-体积单位，COUNT-计数单位，OTHER-其他）
     */
    private String unitType;

    /**
     * 换算比例（相对于基础单位的换算系数）
     */
    private java.math.BigDecimal conversionRate;

    /**
     * 基础单位ID（父单位ID）
     */
    private Long baseUnitId;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}