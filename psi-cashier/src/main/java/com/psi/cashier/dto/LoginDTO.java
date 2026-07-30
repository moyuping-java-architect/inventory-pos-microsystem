package com.psi.cashier.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO {

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 门店编码（可选，指定后只查询该门店的操作员）
     */
    private String shopCode;
}