package com.psi.stock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check_item")
public class StockCheckItemEntity extends BaseEntity {

    private Long checkId;
    private String checkNo;
    private String goodsCode;
    private String skuCode;
    private String skuName;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private BigDecimal conversionRate;
    private BigDecimal bookQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal diffQuantity;
    private BigDecimal unitPrice;
    private BigDecimal bookAmount;
    private BigDecimal actualAmount;
    private BigDecimal diffAmount;
}