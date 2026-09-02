package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock")
public class StockEntity extends BaseEntity {

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

    @TableField("quantity")
    private BigDecimal quantity;

    @TableField("locked_quantity")
    private BigDecimal lockedQuantity;

    @TableField("available_quantity")
    private BigDecimal availableQuantity;

    @TableField("avg_cost_price")
    private BigDecimal avgCostPrice;

    @TableField("total_amount")
    private BigDecimal totalAmount;
}
