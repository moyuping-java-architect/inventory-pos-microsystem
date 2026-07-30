package com.psi.stock.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_batch")
public class StockBatchEntity extends BaseEntity {

    private String warehouseCode;
    private String warehouseName;
    private String goodsCode;
    private String skuCode;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private String batchNo;
    private String productionDate;
    private String expireDate;
    private BigDecimal quantity;
    private BigDecimal lockedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal costPrice;
    private BigDecimal totalAmount;
    private String supplierCode;
    private String supplierName;
}