package com.psi.cashier.dto;

import lombok.Data;

/**
 * 登录结果DTO
 */
@Data
public class LoginResultDTO {

    /**
     * 是否登录成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 用户信息
     */
    private UserInfo user;

    /**
     * Token（用于后续请求验证）
     */
    private String token;

    @Data
    public static class UserInfo {
        /** 用户ID */
        private Long id;
        /** 用户名 */
        private String username;
        /** 真实姓名 */
        private String realName;
        /** 角色：ADMIN-管理员，OPERATOR-收银员 */
        private String role;
        /** 门店编码 */
        private String shopCode;
        /** 租户ID */
        private String tenantId;
    }
}