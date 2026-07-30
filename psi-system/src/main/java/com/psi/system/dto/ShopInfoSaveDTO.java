package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShopInfoSaveDTO implements Serializable {

    private String shopName;

    private String shopCode;

    private String address;

    private String phone;

    private String manager;
}