package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StockTransferSaveDTO implements Serializable {

    private Long id;
    private String docName;
    private String transferNo;
    private String fromWarehouseCode;
    private String fromWarehouseName;
    private String toWarehouseCode;
    private String toWarehouseName;
    private String transferDate;
    private String remark;
    private List<StockTransferItemSaveDTO> items;
}