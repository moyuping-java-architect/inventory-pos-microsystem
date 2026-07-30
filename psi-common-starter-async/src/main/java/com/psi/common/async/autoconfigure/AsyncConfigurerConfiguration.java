package com.psi.common.async.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 全局异步配置器
 * 实现 AsyncConfigurer 接口，配置默认异步执行器和异常处理器
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class AsyncConfigurerConfiguration implements AsyncConfigurer {

    private final ApplicationContext applicationContext;

    @Autowired
    public AsyncConfigurerConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 配置默认异步执行器
     * 所有 @Async 注解方法默认使用此执行器（除非指定其他 bean 名称）
     */
    @Override
    public Executor getAsyncExecutor() {
        Executor executor = applicationContext.getBean("asyncExecutor", Executor.class);
        log.debug("Using TTL async executor as default");
        return executor;
    }

    /**
     * 配置异步任务异常处理器
     * 所有异步任务抛出的未捕获异常都会被此处理器捕获
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error("Async task exception occurred: method={}, params={}", 
                    method.getName(), objects, throwable);
            
            // 可以在这里添加告警逻辑，比如发送通知到监控系统
            // notifyMonitor(throwable, method);
        };
    }

    /**
     * 获取底层线程池（用于监控和管理）
     */
    public ThreadPoolExecutor getUnderlyingExecutor() {
        Executor executor = applicationContext.getBean("asyncExecutor", Executor.class);
        if (executor instanceof ThreadPoolTaskExecutor) {
            return ((ThreadPoolTaskExecutor) executor).getThreadPoolExecutor();
        }
        return null;
    }
}