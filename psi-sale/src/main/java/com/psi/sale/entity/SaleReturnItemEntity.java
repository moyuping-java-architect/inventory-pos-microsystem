package com.psi.sale.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_return_item")
public class SaleReturnItemEntity extends BaseEntity {

    /**
     * 退货单主表ID
     */
    private Long returnId;

    /**
     * 退货单编号
     */
    private String returnNo;

    /**
     * 关联出库单ID
     */
    private Long outId;

    /**
     * 关联出库单编号
     */
    private String outNo;

    /**
     * 关联销售订单编号
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
     * 出库数量
     */
    private BigDecimal outQuantity;

    /**
     * 退货数量
     */
    private BigDecimal returnQuantity;

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
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 有效期
     */
    private String expireDate;

    /**
     * 备注
     */
    private String remark;
}