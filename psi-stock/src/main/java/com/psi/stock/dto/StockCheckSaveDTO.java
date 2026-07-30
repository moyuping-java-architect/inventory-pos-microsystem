package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StockCheckSaveDTO implements Serializable {

    private Long id;
    private String docName;
    private String checkNo;
    private String warehouseCode;
    private String warehouseName;
    private String checkDate;
    private String remark;
    private List<StockCheckItemSaveDTO> items;
}