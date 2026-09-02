package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockFlowQueryDTO implements Serializable {

    private String warehouseCode;
    private String goodsCode;
    private String skuCode;
    private Integer flowType;
    private String sourceNo;
    private String sourceType;
    private String startDate;
    private String endDate;
    private Integer pageNum;
    private Integer pageSize;
}