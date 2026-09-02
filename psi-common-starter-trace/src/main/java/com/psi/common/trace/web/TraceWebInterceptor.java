package com.psi.common.trace.web;

import com.psi.common.context.UserContext;
import com.psi.common.trace.config.TraceProperties;
import com.psi.common.trace.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Web请求追踪拦截器
 * 在HTTP请求入口创建追踪节点
 * 确保每个请求都有唯一的traceId，支持跨服务追踪
 * 
 * @author PSI
 * @version 1.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class TraceWebInterceptor implements HandlerInterceptor {

    private final TraceProperties properties;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                            @NonNull HttpServletResponse response,
                            @NonNull Object handler) {
        if (!properties.isEnabled()) {
            return true;
        }

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        // 从请求头获取 traceId（支持跨服务传递）
        String headerTraceId = request.getHeader(properties.getTraceIdHeader());

        String traceId;
        if (headerTraceId != null && !headerTraceId.isEmpty()) {
            // 使用上游服务传递的 traceId
            traceId = headerTraceId;
            UserContext.setTraceId(traceId);
            log.debug("Received traceId from upstream: {}", traceId);
        } else {
            // 生成新的 traceId
            traceId = TraceUtil.getOrInitTraceId();
        }

        String spanId = TraceUtil.generateSpanId();

        // 将 traceId 设置到响应头，便于客户端追踪
        response.setHeader(properties.getTraceIdHeader(), traceId);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            // 仅在异常场景打印日志
            if (ex != null) {
                String traceId = UserContext.getTraceId();
                log.error("[Trace] Request completed with exception: traceId={}, uri={}, error={}",
                        traceId, request.getRequestURI(), ex.getMessage());
            }
        } finally {
            // 确保上下文一定被清理
            UserContext.clearAll();
        }
    }
}