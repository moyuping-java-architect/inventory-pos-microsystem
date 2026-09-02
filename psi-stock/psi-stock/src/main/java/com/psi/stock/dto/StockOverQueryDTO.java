package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockOverQueryDTO implements Serializable {

    private String overNo;
    private String warehouseCode;
    private String overDate;
    private Integer pageNum;
    private Integer pageSize;
}