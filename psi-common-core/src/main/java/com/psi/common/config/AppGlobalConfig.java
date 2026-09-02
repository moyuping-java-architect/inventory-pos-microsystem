package com.psi.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 应用全局配置
 * 
 * <p>包含应用级别的配置参数
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@Component
public class AppGlobalConfig {

    /**
     * 当前服务名称
     */
    private static String currentServiceName;

    @Value("${spring.application.name:unknown-service}")
    public void initServiceName(String serviceName) {
        currentServiceName = serviceName;
    }

    public static String getCurrentServiceName() {
        return currentServiceName;
    }

    /**
     * JWT 密钥
     */
    @Value("${psi.jwt.secret:psi-jwt-secret-key-2024}")
    private String jwtSecret;

    /**
     * JWT 过期时间（秒）
     */
    @Value("${psi.jwt.expire:86400}")
    private Long jwtExpire;
}