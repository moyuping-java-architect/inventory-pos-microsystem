package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WarehouseInfoQueryDTO implements Serializable {

    private String warehouseName;

    private String warehouseCode;

    private Long shopId;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}