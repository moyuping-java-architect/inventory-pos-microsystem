package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockCheckQueryDTO implements Serializable {

    private String checkNo;
    private String warehouseCode;
    private String checkDate;
    private Integer pageNum;
    private Integer pageSize;
}