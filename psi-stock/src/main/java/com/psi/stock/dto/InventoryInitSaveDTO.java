package com.psi.stock.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InventoryInitSaveDTO implements Serializable {

    private Long id;
    private String docName;
    private String initNo;
    private String warehouseCode;
    private String warehouseName;
    private String initDate;
    private String remark;
    private List<InventoryInitItemSaveDTO> items;
}