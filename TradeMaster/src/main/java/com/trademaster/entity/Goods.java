package com.trademaster.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.trademaster.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods")
public class Goods extends BaseEntity {
    private String goodsCode;
    private String goodsName;
    private Long categoryId;
    private String brand;
    private String unit;
    private String barCode;
    private java.math.BigDecimal purchasePrice;
    private java.math.BigDecimal salePrice;
    private java.math.BigDecimal memberPrice;
    private java.math.BigDecimal stockQty;
    private java.math.BigDecimal minStock;
    private java.math.BigDecimal maxStock;
    private Integer status;
    private String imageUrl;
    private String description;
}
