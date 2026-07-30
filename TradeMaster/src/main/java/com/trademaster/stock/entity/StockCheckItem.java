package com.trademaster.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("stock_check_item")
public class StockCheckItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;

    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String skuCode;

    private String skuName;

    private String unitCode;

    private BigDecimal systemQuantity;

    private BigDecimal actualQuantity;

    private BigDecimal varianceQuantity;

    private BigDecimal unitCostPrice;

    private BigDecimal varianceAmount;

    private String batchNo;

    private LocalDate expireDate;

    private String remark;
}
