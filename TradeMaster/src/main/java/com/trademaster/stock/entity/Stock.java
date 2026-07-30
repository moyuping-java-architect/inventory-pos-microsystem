package com.trademaster.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock")
public class Stock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String warehouseCode;

    private String warehouseName;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String skuCode;

    private String skuName;

    private String unit;

    private BigDecimal quantity;

    private BigDecimal lockedQuantity;

    private BigDecimal availableQuantity;

    private BigDecimal avgCostPrice;

    private BigDecimal totalAmount;

    private String batchNo;

    private LocalDate expiryDate;

    private Integer status;

    @TableLogic
    private Integer delFlag;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
