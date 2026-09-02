package com.psi.system.util;

import io.jsonwebtoken.*;
import com.psi.common.context.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:psi-jwt-secret-key-256-bit-minimum-length-required-for-hs256}")
    private String secret;

    @Value("${jwt.expire:86400000}")
    private Long expire;

    @Value("${jwt.refresh-expire:604800000}")
    private Long refreshExpire;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }
        return new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generateToken(UserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", userInfo.getTenantId());
        claims.put("shopId", userInfo.getShopId());
        claims.put("warehouseId", userInfo.getWarehouseId());
        claims.put("userId", userInfo.getUpdateUserId());
        claims.put("userName", userInfo.getUpdateUserName());
        claims.put("roleId", userInfo.getRoleId());
        claims.put("roleCode", userInfo.getRoleCode());
        claims.put("roleName", userInfo.getRoleName());
        claims.put("permissions", userInfo.getPermissions());

        return Jwts.builder()
                .claims(claims)
                .subject(userInfo.getUpdateUserId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userInfo.getUpdateUserId());

        return Jwts.builder()
                .claims(claims)
                .subject(userInfo.getUpdateUserId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpire))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期");
        } catch (JwtException e) {
            throw new RuntimeException("Token无效");
        }
    }

    public UserInfo getUserInfoFromToken(String token) {
        Claims claims = parseToken(token);
        UserInfo userInfo = new UserInfo();
        userInfo.setTenantId(claims.get("tenantId", String.class));
        userInfo.setShopId(claims.get("shopId", String.class));
        userInfo.setWarehouseId(claims.get("warehouseId", String.class));
        userInfo.setUpdateUserId(claims.get("userId", String.class));
        userInfo.setUpdateUserName(claims.get("userName", String.class));
        userInfo.setRoleId(claims.get("roleId", String.class));
        userInfo.setRoleCode(claims.get("roleCode", String.class));
        userInfo.setRoleName(claims.get("roleName", String.class));
        userInfo.setPermissions(claims.get("permissions", String.class));
        return userInfo;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getExpire() {
        return expire;
    }
}