package com.psi.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockWarnDTO {

    private Long id;

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
