package com.trademaster.sale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("sale_order_item")
public class SaleOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String skuCode;

    private String skuName;

    private String unitCode;

    private BigDecimal conversionRate;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal taxRate;

    private BigDecimal discountRate;

    private BigDecimal amount;

    private String remark;
}
