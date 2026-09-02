package com.psi.common.util;

import com.psi.common.config.AppGlobalConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 * 
 * <p>提供 JWT Token 的生成、解析和验证功能
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtUtils {

    private static SecretKey secretKey;

    @Autowired
    public JwtUtils(AppGlobalConfig config) {
        String secret = config.getJwtSecret() != null ? config.getJwtSecret() : "psi-jwt-secret-key-256-bit-minimum-length-required-for-hs256";
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     * 
     * @param claims 用户信息
     * @param expireTime 过期时间（毫秒）
     * @return JWT Token
     */
    public static String generateToken(Map<String, Object> claims, long expireTime) {
        try {
            return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
        } catch (Exception e) {
            log.error("Failed to generate JWT token", e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * 解析 Token
     * 
     * @param token JWT Token
     * @return 用户信息 Claims，如果验证失败返回 null
     */
    public static Map<String, Object> parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        try {
            Jws<Claims> jws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            
            return jws.getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 从 Token 中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public static String getUserId(String token) {
        Map<String, Object> claims = parseToken(token);
        return claims != null ? (String) claims.get("userId") : null;
    }

    /**
     * 从 Token 中获取租户ID
     * 
     * @param token JWT Token
     * @return 租户ID
     */
    public static String getTenantId(String token) {
        Map<String, Object> claims = parseToken(token);
        return claims != null ? (String) claims.get("tenantId") : null;
    }

    /**
     * 获取 Token 过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     */
    public static Date getExpiration(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return jws.getPayload().getExpiration();
        } catch (Exception e) {
            return null;
        }
    }
}