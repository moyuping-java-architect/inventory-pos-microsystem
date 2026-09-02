package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_over_item")
public class StockOverItemEntity extends BaseEntity {

    private Long overId;
    private String overNo;
    private String goodsCode;
    private String skuCode;
    private String skuName;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private BigDecimal conversionRate;
    private BigDecimal overQuantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private String overReason;
}