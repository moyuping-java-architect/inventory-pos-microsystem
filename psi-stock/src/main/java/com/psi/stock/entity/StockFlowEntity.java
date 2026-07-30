package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_flow")
public class StockFlowEntity extends BaseEntity {

    @TableField("warehouse_code")
    private String warehouseCode;

    @TableField("warehouse_name")
    private String warehouseName;

    @TableField("goods_code")
    private String goodsCode;

    @TableField("sku_code")
    private String skuCode;

    @TableField("goods_name")
    private String goodsName;

    @TableField("goods_spec")
    private String goodsSpec;

    @TableField("unit")
    private String unit;

    @TableField("flow_type")
    private Integer flowType;

    @TableField("in_quantity")
    private BigDecimal inQuantity;

    @TableField("out_quantity")
    private BigDecimal outQuantity;

    @TableField("before_quantity")
    private BigDecimal beforeQuantity;

    @TableField("after_quantity")
    private BigDecimal afterQuantity;

    @TableField("cost_price")
    private BigDecimal costPrice;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("source_no")
    private String sourceNo;

    @TableField("source_type")
    private String sourceType;

    @TableField("remark")
    private String remark;
}
