package com.psi.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
/**
 * 全局过滤器配置
 * 
 * <p>包含跨域处理和请求日志记录
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class GlobalFilterConfig {

    /**
     * 允许的来源
     */
    private static final String ALLOWED_ORIGIN = "*";

    /**
     * 允许的请求头
     */
    private static final String ALLOWED_HEADERS = "*";

    /**
     * 允许的方法
     */
    private static final String ALLOWED_METHODS = "GET,POST,PUT,DELETE,PATCH,OPTIONS";

    /**
     * 暴露的响应头
     */
    private static final String EXPOSED_HEADERS = "Authorization,X-Tenant-Id,X-User-Id,X-Role-Id";

    /**
     * 请求日志过滤器
     */
    @Bean
    public GlobalFilter loggingFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod().name();
            String remoteAddr = request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

            log.info("Gateway request: {} {} from {}", method, path, remoteAddr);

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                org.springframework.http.HttpStatusCode statusCode = response.getStatusCode();
                HttpStatus status = statusCode != null ? statusCode instanceof HttpStatus ? (HttpStatus) statusCode : HttpStatus.valueOf(statusCode.value()) : HttpStatus.INTERNAL_SERVER_ERROR;
                log.info("Gateway response: {} {} -> {}", method, path, status);
            }));
        };
    }

    /**
     * 跨域处理过滤器
     */
    @Bean
    public GlobalFilter corsFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 预检请求直接返回
            if (HttpMethod.OPTIONS.equals(request.getMethod())) {
                ServerHttpResponse response = exchange.getResponse();
                setCorsHeaders(response);
                return Mono.empty();
            }

            // 设置响应头
            exchange.getResponse().beforeCommit(() -> {
                ServerHttpResponse response = exchange.getResponse();
                setCorsHeaders(response);
                return Mono.empty();
            });

            return chain.filter(exchange);
        };
    }

    /**
     * 设置跨域响应头
     */
    private void setCorsHeaders(ServerHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN);
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSED_HEADERS);
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    }

    /**
     * 请求ID过滤器（用于链路追踪）
     */
    @Bean
    public GlobalFilter requestIdFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 获取或生成请求ID
            String requestId = request.getHeaders().getFirst("X-Request-Id");
            if (!org.springframework.util.StringUtils.hasText(requestId)) {
                requestId = java.util.UUID.randomUUID().toString();
            }

            // 将请求ID添加到请求头
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-Id", requestId)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }
}