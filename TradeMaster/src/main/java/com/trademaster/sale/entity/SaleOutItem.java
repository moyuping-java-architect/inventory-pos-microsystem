package com.trademaster.sale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("sale_out_item")
public class SaleOutItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long outId;

    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String skuCode;

    private String skuName;

    private String unitCode;

    private BigDecimal conversionRate;

    private BigDecimal outQuantity;

    private BigDecimal unitPrice;

    private BigDecimal taxRate;

    private BigDecimal amount;

    private String batchNo;

    private LocalDate expireDate;

    private String remark;
}
