package com.psi.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "psi:ratelimit:";

    public RateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String clientIp = getClientIp(request);
            
            String key = buildKey(clientIp, path);
            int limit = config.limit > 0 ? config.limit : 100;
            int windowSeconds = config.windowSeconds > 0 ? config.windowSeconds : 60;

            return redisTemplate.opsForValue().increment(key)
                    .flatMap(count -> {
                        if (count == 1) {
                            return redisTemplate.expire(key, Duration.ofSeconds(windowSeconds))
                                    .then(Mono.just(count));
                        }
                        return Mono.just(count);
                    })
                    .flatMap(count -> {
                        if (count > limit) {
                            log.warn("Rate limit exceeded for path: {}, ip: {}, count: {}", path, clientIp, count);
                            return rateLimitResponse(exchange);
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(e -> {
                        log.warn("Rate limit redis error: {}", e.getMessage());
                        return chain.filter(exchange);
                    });
        };
    }

    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddress() != null ? 
                    request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String buildKey(String clientIp, String path) {
        String pathKey = path.startsWith("/psi/") ? path.substring(4) : path;
        if (pathKey.contains("/")) {
            pathKey = pathKey.split("/")[0];
        }
        return RATE_LIMIT_PREFIX + pathKey + ":" + clientIp;
    }

    private Mono<Void> rateLimitResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Retry-After", "60");
        
        String body = "{\"code\":429,\"message\":\"Too many requests, please try again later\",\"data\":null}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        private int limit = 100;
        private int windowSeconds = 60;

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        public int getWindowSeconds() { return windowSeconds; }
        public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }
    }
}
