package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShopInfoDTO implements Serializable {

    private Long id;

    private String shopName;

    private String shopCode;

    private String address;

    private String phone;

    private String manager;

    private Integer status;

    private String deptNames;
}