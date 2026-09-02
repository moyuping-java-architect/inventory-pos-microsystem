package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_order_item")
public class SaleOrderItemEntity extends BaseEntity {

    /**
     * 订单主表ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 行号
     */
    private Integer itemNo;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品编码
     */
    private String goodsCode;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU名称
     */
    private String skuName;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品规格型号
     */
    private String goodsSpec;

    /**
     * 计量单位编码
     */
    private String unitCode;

    /**
     * 销售单位到库存基础单位的换算率
     */
    private BigDecimal conversionRate;

    /**
     * 销售数量
     */
    private BigDecimal quantity;

    /**
     * 单价（不含税）
     */
    private BigDecimal unitPrice;

    /**
     * 金额（不含税）
     */
    private BigDecimal amount;

    /**
     * 税率
     */
    private BigDecimal taxRate;

    /**
     * 标价是否含税(0:否 1:是)
     */
    private Integer isTaxInclusive;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 净金额（含税）
     */
    private BigDecimal netAmount;

    /**
     * 备注
     */
    private String remark;
}