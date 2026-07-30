package com.trademaster.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_flow")
public class StockFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String warehouseCode;

    private String goodsCode;

    private String skuCode;

    private String flowType;

    private BigDecimal quantity;

    private BigDecimal beforeQuantity;

    private BigDecimal afterQuantity;

    private BigDecimal avgCostPrice;

    private BigDecimal amount;

    private String sourceNo;

    private String sourceType;

    private String remark;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
