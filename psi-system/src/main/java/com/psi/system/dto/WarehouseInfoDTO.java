package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WarehouseInfoDTO implements Serializable {

    private Long id;

    private Long shopId;

    private String warehouseName;

    private String warehouseCode;

    private String address;

    private BigDecimal capacity;

    private String manager;

    private String phone;

    private Integer status;
}