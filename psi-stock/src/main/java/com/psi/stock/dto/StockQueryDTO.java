package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockQueryDTO implements Serializable {

    private String warehouseCode;
    private String goodsCode;
    private String skuCode;
    private String goodsName;
    private Integer pageNum;
    private Integer pageSize;
}