package com.psi.common.trace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 追踪配置属性类
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "psi.trace")
@RefreshScope
public class TraceProperties {

    /**
     * 是否启用追踪功能（默认启用）
     */
    private boolean enabled = true;

    /**
     * traceId 在请求头中的键名
     */
    private String traceIdHeader = "X-Trace-Id";

    /**
     * spanId 在请求头中的键名
     */
    private String spanIdHeader = "X-Span-Id";

    /**
     * 父spanId 在请求头中的键名
     */
    private String parentSpanIdHeader = "X-Parent-Span-Id";

    /**
     * 服务名称（自动获取spring.application.name）
     */
    private String serviceName;

    /**
     * 方法超时时间阈值（毫秒），超过此时间将记录警告日志
     * 默认值：30000毫秒（30秒）
     */
    private long methodTimeoutThreshold = 30000L;
}