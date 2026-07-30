package com.trademaster.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

@Configuration
@EnableAsync
public class WorkflowAsyncConfig {

    @Bean("workflowAsyncExecutor")
    public Executor workflowAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("workflow-async-");
        executor.setThreadFactory(virtualThreadFactory());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    private ThreadFactory virtualThreadFactory() {
        return Thread.ofVirtual().name("workflow-virtual-", 0).factory();
    }
}
