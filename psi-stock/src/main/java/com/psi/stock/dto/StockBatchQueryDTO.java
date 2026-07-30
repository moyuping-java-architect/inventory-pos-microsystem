package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockBatchQueryDTO implements Serializable {

    private String warehouseCode;
    private String goodsCode;
    private String batchNo;
    private String supplierCode;
    private String expireDateStart;
    private String expireDateEnd;
    private Integer pageNum;
    private Integer pageSize;
}