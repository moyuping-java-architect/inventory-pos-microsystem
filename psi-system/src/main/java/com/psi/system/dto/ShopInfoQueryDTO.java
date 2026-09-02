package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShopInfoQueryDTO implements Serializable {

    private String shopName;

    private String shopCode;

    private Integer status;

    private Integer pageNum;

    private Integer pageSize;
}