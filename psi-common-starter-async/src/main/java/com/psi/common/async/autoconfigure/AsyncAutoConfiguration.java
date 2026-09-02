package com.psi.common.async.autoconfigure;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.psi.common.async.config.AsyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步自动配置类
 * 配置 TTL 租户上下文透传的异步执行器
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(AsyncProperties.class)
@ConditionalOnProperty(prefix = "psi.async", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.psi.common.async.facade")
@RequiredArgsConstructor
public class AsyncAutoConfiguration {

    private final AsyncProperties properties;

    /**
     * 配置 TTL 线程池执行器
     * 使用 TransmittableThreadLocal 包装线程池，确保租户上下文在线程池环境下正确传递
     * 
     * @return Executor 实例
     */
    @Bean({"asyncExecutor", "mqAsyncExecutor"})
    @ConditionalOnMissingBean(name = {"asyncExecutor", "mqAsyncExecutor"})
    public Executor asyncExecutor() {
        AsyncProperties.ThreadPoolConfig config = properties.getThreadPool();
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(config.getCorePoolSize());
        
        // 最大线程数
        executor.setMaxPoolSize(config.getMaxPoolSize());
        
        // 队列容量
        executor.setQueueCapacity(config.getQueueCapacity());
        
        // 线程名前缀
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        
        // 空闲线程存活时间
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        
        // 是否允许核心线程超时
        executor.setAllowCoreThreadTimeOut(config.isAllowCoreThreadTimeout());
        
        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化
        executor.initialize();

        // 使用 TTL 包装线程池，确保租户上下文传递
        Executor ttlExecutor = TtlExecutors.getTtlExecutor(executor);
        
        log.info("Async executor initialized with TTL wrapper: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                config.getCorePoolSize(), config.getMaxPoolSize(), config.getQueueCapacity());
        
        return ttlExecutor;
    }
}