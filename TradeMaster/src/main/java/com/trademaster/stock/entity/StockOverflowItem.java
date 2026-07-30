package com.trademaster.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("stock_overflow_item")
public class StockOverflowItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long overflowId;

    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String skuCode;

    private String skuName;

    private String unitCode;

    private BigDecimal overflowQuantity;

    private BigDecimal unitCostPrice;

    private BigDecimal amount;

    private String batchNo;

    private LocalDate expireDate;

    private String remark;
}
