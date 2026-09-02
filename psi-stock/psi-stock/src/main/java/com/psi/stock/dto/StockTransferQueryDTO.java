package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockTransferQueryDTO implements Serializable {

    private String transferNo;
    private String fromWarehouseCode;
    private String toWarehouseCode;
    private String transferDate;
    private Integer pageNum;
    private Integer pageSize;
}