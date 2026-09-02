package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_inventory_init_item")
public class InventoryInitItemEntity extends BaseEntity {

    private Long initId;
    private String initNo;
    private String goodsCode;
    private String skuCode;
    private String skuName;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private BigDecimal conversionRate;
    private BigDecimal initQuantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private BigDecimal taxAmount;
}