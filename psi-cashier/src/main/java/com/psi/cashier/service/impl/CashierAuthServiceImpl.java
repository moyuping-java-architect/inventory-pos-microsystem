package com.psi.cashier.service.impl;

import com.psi.cashier.dto.LoginDTO;
import com.psi.cashier.dto.LoginResultDTO;
import com.psi.cashier.entity.OperatorEntity;
import com.psi.cashier.mapper.OperatorMapper;
import com.psi.cashier.service.CashierAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 收银认证服务实现类
 * 基于 operator 表实现登录验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashierAuthServiceImpl implements CashierAuthService {

    private final OperatorMapper operatorMapper;

    /**
     * Token存储（单机场景使用内存）
     * key: token, value: 用户信息
     */
    private final Map<String, LoginResultDTO.UserInfo> tokenStore = new ConcurrentHashMap<>();

    @Override
    public LoginResultDTO login(LoginDTO dto) {
        LoginResultDTO result = new LoginResultDTO();

        // 查询操作员（根据用户名和门店编码）
        OperatorEntity operator = operatorMapper.selectByUsernameAndShopCode(
                dto.getUsername(),
                dto.getShopCode()
        );

        // 如果没有指定门店编码，只按用户名查询
        if (operator == null && dto.getShopCode() == null) {
            operator = operatorMapper.selectByUsername(dto.getUsername());
        }

        if (operator == null) {
            result.setSuccess(false);
            result.setMessage("用户名不存在或该门店无此操作员");
            return result;
        }

        // 验证密码
        if (!operator.getPassword().equals(dto.getPassword())) {
            result.setSuccess(false);
            result.setMessage("密码错误");
            return result;
        }

        // 生成Token
        String token = UUID.randomUUID().toString();

        // 构建用户信息
        LoginResultDTO.UserInfo userInfo = new LoginResultDTO.UserInfo();
        userInfo.setId(Long.valueOf(operator.getOperatorId()));
        userInfo.setUsername(operator.getUsername());
        userInfo.setRealName(operator.getRealName());
        userInfo.setRole(operator.getRole() == 2 ? "ADMIN" : "OPERATOR");
        userInfo.setShopCode(operator.getShopCode());
        userInfo.setTenantId(operator.getTenantId());

        // 存储Token
        tokenStore.put(token, userInfo);

        log.info("操作员登录成功，用户名：{}，门店：{}，角色：{}",
                operator.getUsername(), operator.getShopCode(),
                operator.getRole() == 2 ? "管理员" : "收银员");

        result.setSuccess(true);
        result.setMessage("登录成功");
        result.setUser(userInfo);
        result.setToken(token);

        return result;
    }

    @Override
    public void logout(String token) {
        LoginResultDTO.UserInfo user = tokenStore.remove(token);
        if (user != null) {
            log.info("操作员登出，用户名：{}", user.getUsername());
        }
    }

    @Override
    public boolean validateToken(String token) {
        return tokenStore.containsKey(token);
    }

    @Override
    public LoginResultDTO.UserInfo getCurrentUser(String token) {
        return tokenStore.get(token);
    }
}