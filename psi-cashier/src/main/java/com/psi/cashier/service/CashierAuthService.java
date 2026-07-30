package com.psi.cashier.service;

import com.psi.cashier.dto.LoginDTO;
import com.psi.cashier.dto.LoginResultDTO;

/**
 * 收银认证服务接口
 */
public interface CashierAuthService {

    /**
     * 用户登录
     */
    LoginResultDTO login(LoginDTO dto);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 验证Token
     */
    boolean validateToken(String token);

    /**
     * 获取当前登录用户信息
     */
    LoginResultDTO.UserInfo getCurrentUser(String token);
}