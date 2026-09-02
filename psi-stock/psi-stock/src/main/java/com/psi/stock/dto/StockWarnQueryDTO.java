package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockWarnQueryDTO implements Serializable {

    private String warehouseCode;

    private String goodsCode;

    private String skuCode;

    private String goodsName;

    private Integer warnType;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
