package com.psi.gateway.filter;

import com.psi.common.constant.TenantMdcConstant;
import com.psi.common.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 认证过滤器
 * 
 * <p>验证请求中的 JWT Token，提取用户信息并传递给下游服务
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    /**
     * 无需认证的路径
     */
    private static final List<String> WHITE_LIST = List.of(
        "/psi/admin/login",
        "/psi/admin/register",
        "/psi/admin/logout",
        "/actuator/**",
        "/health"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            
            // 白名单路径直接放行
            if (isWhiteList(path)) {
                log.debug("White list path: {}", path);
                return chain.filter(exchange);
            }

            // 获取 Token
            String token = extractToken(request);
            if (!StringUtils.hasText(token)) {
                log.warn("No token found in request: {}", path);
                return unauthorizedResponse(exchange);
            }

            // 验证 Token
            try {
                Map<String, Object> claims = JwtUtils.parseToken(token);
                if (claims == null) {
                    log.warn("Invalid token: {}", path);
                    return unauthorizedResponse(exchange);
                }

                // 从请求头获取用户信息（消息头首次设置是在登录成功后设置的），如果请求头为空则从token claims中获取
                ServerHttpRequest.Builder requestBuilder = request.mutate();
                
                // TraceID - 如果请求头中没有则生成一个新的
                String traceId = getHeaderValue(request, TenantMdcConstant.HEADER_TRACE_ID);
                if (!StringUtils.hasText(traceId)) {
                    traceId = generateTraceId();
                }
                requestBuilder.header(TenantMdcConstant.HEADER_TRACE_ID, traceId);
                
                // UserInfo 基础字段 - 优先从请求头获取，否则从token claims获取
                setHeaderIfNotEmpty(requestBuilder, TenantMdcConstant.HEADER_TENANT_ID, getHeaderValue(request, TenantMdcConstant.HEADER_TENANT_ID), getStringValue(claims, "tenantId"));
                setHeaderIfNotEmpty(requestBuilder, TenantMdcConstant.HEADER_SHOP_ID, getHeaderValue(request, TenantMdcConstant.HEADER_SHOP_ID), getStringValue(claims, "shopId"));
                setHeaderIfNotEmpty(requestBuilder, TenantMdcConstant.HEADER_WAREHOUSE_ID, getHeaderValue(request, TenantMdcConstant.HEADER_WAREHOUSE_ID), getStringValue(claims, "warehouseId"));
                setHeaderIfNotEmpty(requestBuilder, "X-User-Id", getHeaderValue(request, "X-User-Id"), getStringValue(claims, "userId"));
                setHeaderIfNotEmpty(requestBuilder, "X-User-Name", getHeaderValue(request, "X-User-Name"), getStringValue(claims, "userName"));
                setHeaderIfNotEmpty(requestBuilder, TenantMdcConstant.HEADER_ROLE_ID, getHeaderValue(request, TenantMdcConstant.HEADER_ROLE_ID), getStringValue(claims, "roleId"));
                setHeaderIfNotEmpty(requestBuilder, TenantMdcConstant.HEADER_ROLE_CODE, getHeaderValue(request, TenantMdcConstant.HEADER_ROLE_CODE), getStringValue(claims, "roleCode"));
                setHeaderIfNotEmpty(requestBuilder, TenantMdcConstant.HEADER_ROLE_NAME, getHeaderValue(request, TenantMdcConstant.HEADER_ROLE_NAME), getStringValue(claims, "roleName"));
                setHeaderIfNotEmpty(requestBuilder, TenantMdcConstant.HEADER_PERMISSIONS, getHeaderValue(request, TenantMdcConstant.HEADER_PERMISSIONS), getStringValue(claims, "permissions"));
                
                // 扩展字段
                requestBuilder.header("X-Token", token);
                setHeaderIfNotEmpty(requestBuilder, "X-Real-Name", getHeaderValue(request, "X-Real-Name"), getStringValue(claims, "realName"));
                setHeaderIfNotEmpty(requestBuilder, "X-Email", getHeaderValue(request, "X-Email"), getStringValue(claims, "email"));
                setHeaderIfNotEmpty(requestBuilder, "X-Phone", getHeaderValue(request, "X-Phone"), getStringValue(claims, "phone"));
                setHeaderIfNotEmpty(requestBuilder, "X-Dept-Id", getHeaderValue(request, "X-Dept-Id"), getStringValue(claims, "deptId"));
                setHeaderIfNotEmpty(requestBuilder, "X-Dept-Name", getHeaderValue(request, "X-Dept-Name"), getStringValue(claims, "deptName"));
                
                ServerHttpRequest modifiedRequest = requestBuilder.build();

                String tenantId = getHeaderValue(request, TenantMdcConstant.HEADER_TENANT_ID);
                if (tenantId == null || tenantId.isEmpty()) {
                    tenantId = getStringValue(claims, "tenantId");
                }
                String userId = getHeaderValue(request, "X-User-Id");
                if (userId == null || userId.isEmpty()) {
                    userId = getStringValue(claims, "userId");
                }
                log.debug("Token validated for path: {}, userId={}, tenantId={}", 
                        path, userId, tenantId);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("Token validation error: {}", e.getMessage());
                return unauthorizedResponse(exchange);
            }
        };
    }

    /**
     * 从请求头获取值
     */
    private String getHeaderValue(ServerHttpRequest request, String headerName) {
        return request.getHeaders().getFirst(headerName);
    }

    /**
     * 设置请求头（如果值不为空）
     */
    private void setHeaderIfNotEmpty(ServerHttpRequest.Builder builder, String headerName, String primaryValue, String fallbackValue) {
        if (StringUtils.hasText(primaryValue)) {
            builder.header(headerName, primaryValue);
        } else if (StringUtils.hasText(fallbackValue)) {
            builder.header(headerName, fallbackValue);
        }
    }

    /**
     * 获取 claims 中的字符串值
     */
    private String getStringValue(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * 生成 TraceID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 判断路径是否在白名单中
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 从请求中提取 Token
     */
    private String extractToken(ServerHttpRequest request) {
        // 从 Authorization header 获取
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 从请求参数获取
        String tokenParam = request.getQueryParams().getFirst("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String body = "{\"code\":401,\"message\":\"Unauthorized - Invalid or missing token\",\"data\":null}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    /**
     * 配置类
     */
    public static class Config {
        // 可配置参数（如是否启用等）
    }
}