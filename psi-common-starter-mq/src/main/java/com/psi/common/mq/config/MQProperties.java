package com.psi.common.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * MQ 配置属性类
 * 支持 TTL 租户上下文透传配置
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "psi.mq")
@RefreshScope
public class MQProperties {

    /**
     * 是否启用 TTL 租户上下文透传
     */
    private boolean tenantPropagationEnabled = true;

    /**
     * 是否强制要求发送消息时必须有租户上下文
     * true: 如果上下文为空则抛出异常
     * false: 上下文为空时记录警告并继续发送（默认）
     */
    private boolean requireTenantContext = false;

    /**
     * 租户ID 在消息头中的键名
     */
    private String tenantIdHeader = "X-Tenant-Id";

    /**
     * 用户ID 在消息头中的键名
     */
    private String userIdHeader = "X-User-Id";

    /**
     * 消息发送超时时间（毫秒）
     */
    private int sendTimeout = 10000;

    /**
     * 线程池配置
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    /**
     * 线程池配置内部类
     */
    @Data
    public static class ThreadPoolConfig {
        
        /**
         * 是否启用异步发送
         */
        private boolean asyncEnabled = false;

        /**
         * 核心线程数
         */
        private int corePoolSize = 4;

        /**
         * 最大线程数
         */
        private int maxPoolSize = 8;

        /**
         * 队列容量
         */
        private int queueCapacity = 1000;

        /**
         * 线程名前缀
         */
        private String threadNamePrefix = "mq-sender-";
    }
}