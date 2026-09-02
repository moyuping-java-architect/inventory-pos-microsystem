package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StockLossSaveDTO implements Serializable {

    private Long id;
    private String docName;
    private String lossNo;
    private String warehouseCode;
    private String warehouseName;
    private String lossDate;
    private String remark;
    private List<StockLossItemSaveDTO> items;
}