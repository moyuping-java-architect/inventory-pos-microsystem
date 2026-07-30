package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StockOverSaveDTO implements Serializable {

    private Long id;
    private String docName;
    private String overNo;
    private String warehouseCode;
    private String warehouseName;
    private String overDate;
    private String remark;
    private List<StockOverItemSaveDTO> items;
}