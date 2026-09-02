package com.psi.goods.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品调价单明细表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_adjust_price_item")
public class AdjustPriceItemEntity extends BaseEntity {

    /**
     * 调价单ID
     */
    @TableField("adjust_id")
    private Long adjustId;

    /**
     * 商品编码
     */
    @TableField("goods_code")
    private String goodsCode;

    /**
     * 商品名称
     */
    @TableField("goods_name")
    private String goodsName;

    /**
     * SKU编码
     */
    @TableField("sku_code")
    private String skuCode;

    /**
     * 商品规格
     */
    @TableField("goods_spec")
    private String goodsSpec;

    /**
     * 单位
     */
    @TableField("goods_unit")
    private String goodsUnit;

    /**
     * 原销售价
     */
    @TableField("old_price")
    private BigDecimal oldPrice;

    /**
     * 新销售价
     */
    @TableField("new_price")
    private BigDecimal newPrice;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
