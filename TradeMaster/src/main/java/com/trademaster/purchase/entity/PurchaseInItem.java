package com.trademaster.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("purchase_in_item")
public class PurchaseInItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inId;

    private Long goodsId;

    private String goodsCode;

    private String goodsName;

    private String goodsSpec;

    private String skuCode;

    private String skuName;

    private String unitCode;

    private BigDecimal conversionRate;

    private BigDecimal inQuantity;

    private BigDecimal unitPrice;

    private BigDecimal taxRate;

    private BigDecimal amount;

    private String batchNo;

    private LocalDate expireDate;

    private String remark;
}
