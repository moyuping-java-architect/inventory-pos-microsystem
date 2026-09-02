package com.psi.stock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.psi.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_warn")
public class StockWarnEntity extends BaseEntity {

    private String warehouseCode;

    private String warehouseName;

    private String goodsCode;

    private String goodsName;

    private String skuCode;

    private String goodsSpec;

    private String unit;

    private BigDecimal minStockQty;

    private BigDecimal maxStockQty;

    private BigDecimal currentQty;

    private Integer warnType;

    private Integer status;

    private String remark;
}
