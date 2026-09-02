package com.psi.common.trace.autoconfigure;

import com.psi.common.trace.aspectj.TraceLtwAspect;
import com.psi.common.trace.aspectj.TraceLtwConfigurer;
import com.psi.common.trace.async.TraceAsyncAspect;
import com.psi.common.trace.config.TraceLogProperties;
import com.psi.common.trace.config.TraceProperties;
import com.psi.common.trace.feign.TraceFeignInterceptor;
import com.psi.common.trace.mq.TraceMqConsumerAspect;
import com.psi.common.trace.mq.TraceMqProducerInterceptor;
import com.psi.common.trace.scheduled.TraceScheduledAspect;
import com.psi.common.trace.web.TraceWebInterceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 追踪自动配置类（方案二：AspectJ LTW 拦截所有方法）
 * Spring Boot 3.x 自动配置方式
 * 核心设计：
 * 1. 使用 AspectJ LTW 统一拦截所有方法（private/protected/public）
 * 2. Web/Feign/MQ 拦截器负责 traceId 的传递和初始化
 * 3. LTW 切面负责超时检测和异常处理
 * 
 * @author PSI
 * @version 2.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({TraceProperties.class, TraceLogProperties.class})
@ConditionalOnProperty(prefix = "psi.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceAutoConfiguration {

    /**
     * 配置 Web 拦截器（入口请求 traceId 初始化）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    public WebMvcConfigurer traceWebMvcConfigurer(TraceProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(new TraceWebInterceptor(properties))
                        .addPathPatterns("/**");
            }
        };
    }

    /**
     * 配置 Feign 拦截器（跨服务传递 traceId）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(FeignClient.class)
    public TraceFeignInterceptor traceFeignInterceptor(TraceProperties properties) {
        return new TraceFeignInterceptor(properties);
    }

    /**
     * 配置 MQ 生产者拦截器（消息传递 traceId）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RabbitTemplate.class)
    public TraceMqProducerInterceptor traceMqProducerInterceptor(TraceProperties properties) {
        return new TraceMqProducerInterceptor(properties);
    }

    /**
     * 配置 MQ 消费者切面（消息消费追踪，上下文清理）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RabbitTemplate.class)
    public TraceMqConsumerAspect traceMqConsumerAspect(TraceProperties properties) {
        return new TraceMqConsumerAspect(properties);
    }

    /**
     * 配置异步方法追踪切面（支持虚拟线程）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.scheduling.annotation.Async")
    public TraceAsyncAspect traceAsyncAspect(TraceProperties properties) {
        return new TraceAsyncAspect(properties);
    }

    /**
     * 配置定时任务追踪切面（支持 @Scheduled 注解）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.scheduling.annotation.Scheduled")
    public TraceScheduledAspect traceScheduledAspect(TraceProperties properties) {
        return new TraceScheduledAspect(properties);
    }

    /**
     * 配置 AspectJ LTW 切面（核心：拦截所有方法，超时检测，异常处理）
     * 覆盖范围：private/protected/public/package-private 所有方法
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceLtwAspect traceLtwAspect() {
        return new TraceLtwAspect();
    }

    /**
     * 配置 AspectJ LTW 配置器（初始化 properties）
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceLtwConfigurer traceLtwConfigurer(TraceProperties properties) {
        return new TraceLtwConfigurer(properties);
    }

    /**
     * 初始化服务名称
     */
    @Bean
    public String traceServiceName(@Value("${spring.application.name:unknown-service}") String appName,
                                   TraceProperties properties) {
        if (properties.getServiceName() == null || properties.getServiceName().isEmpty()) {
            properties.setServiceName(appName);
        }
        log.info("[Trace] Trace starter initialized (LTW mode): serviceName={}", properties.getServiceName());
        return properties.getServiceName();
    }
}