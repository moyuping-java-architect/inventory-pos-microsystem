package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockLossQueryDTO implements Serializable {

    private String lossNo;
    private String warehouseCode;
    private String lossDate;
    private Integer pageNum;
    private Integer pageSize;
}