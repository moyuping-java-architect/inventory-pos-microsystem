package com.psi.flow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 异步任务线程池配置
 *
 * <p>使用 JDK 21 虚拟线程执行异步任务，适合 I/O 密集型场景（如 MQ 发送、Feign 调用）。
 * 虚拟线程由 JVM 调度，创建/切换成本极低，不需要传统线程池的 core/max/queue 配置。</p>
 */
@Configuration
public class AsyncConfig {

    /**
     * 流程完成 MQ 发送线程池（基于虚拟线程）
     */
    @Bean("flowMqExecutor")
    public Executor flowMqExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
