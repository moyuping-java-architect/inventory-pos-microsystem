package com.psi.common.async.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 异步配置属性类
 * 支持 JDK 虚拟线程、传统线程池双模式
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "psi.async")
@RefreshScope
public class AsyncProperties {

    /**
     * 是否启用异步执行器
     */
    private boolean enabled = true;

    /**
     * 是否启用租户上下文透传
     */
    private boolean tenantPropagationEnabled = true;

    /**
     * 运行模式: STANDALONE(单机模式), MICROSERVICE(微服务模式)
     */
    private RunMode runMode = RunMode.MICROSERVICE;

    /**
     * 是否启用虚拟线程（JDK 21+）
     */
    private boolean virtualThreadEnabled = true;

    /**
     * 虚拟线程配置
     */
    private VirtualThreadConfig virtualThread = new VirtualThreadConfig();

    /**
     * 线程池配置（传统线程池模式使用）
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    /**
     * 运行模式枚举
     */
    public enum RunMode {
        /**
         * 单机模式：使用本地线程池/虚拟线程
         */
        STANDALONE,
        /**
         * 微服务模式：结合MQ异步处理
         */
        MICROSERVICE
    }

    /**
     * 虚拟线程配置
     */
    @Data
    public static class VirtualThreadConfig {
        /**
         * 虚拟线程名前缀
         */
        private String threadNamePrefix = "virtual-async-";

        /**
         * 虚拟线程批量提交时的并发度（默认 CPU核心数）
         */
        private int batchConcurrency = Runtime.getRuntime().availableProcessors();

        /**
         * 单个虚拟线程超时时间（毫秒），0 表示不限制
         */
        private long timeoutMillis = 300000L; // 5分钟

        /**
         * 是否启用线程复用（通过虚拟线程调度器）
         */
        private boolean threadReuseEnabled = true;
    }

    /**
     * 线程池配置（传统模式）
     */
    @Data
    public static class ThreadPoolConfig {
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
        private String threadNamePrefix = "async-exec-";

        /**
         * 空闲线程存活时间（秒）
         */
        private int keepAliveSeconds = 60;

        /**
         * 是否允许核心线程超时
         */
        private boolean allowCoreThreadTimeout = false;
    }
}