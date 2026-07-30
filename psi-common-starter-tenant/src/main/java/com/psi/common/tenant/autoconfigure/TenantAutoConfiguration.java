package com.psi.common.tenant.autoconfigure;

import com.psi.common.tenant.config.TenantProperties;
import com.psi.common.tenant.config.TenantWebMvcConfig;
import com.psi.common.tenant.web.TenantContextWebInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 租户自动配置类
 * 
 * @author PSI
 * @version 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(TenantProperties.class)
@ConditionalOnProperty(prefix = "psi.tenant", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TenantAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TenantAutoConfiguration.class);

    private final TenantProperties properties;

    public TenantAutoConfiguration(TenantProperties properties) {
        this.properties = properties;
    }

    /**
     * 配置租户上下文拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantContextWebInterceptor tenantContextInterceptor() {
        return new TenantContextWebInterceptor(properties);
    }

    /**
     * 配置Web MVC拦截器注册（仅在Web环境中）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    public TenantWebMvcConfig tenantWebMvcConfig(TenantContextWebInterceptor interceptor) {
        return new TenantWebMvcConfig(interceptor);
    }

    /**
     * 配置MQ生产者租户拦截器（仅当Spring AMQP存在时）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.amqp.core.MessagePostProcessor")
    public Object tenantMqProducerInterceptor() throws Exception {
        Class<?> clazz = Class.forName("com.psi.common.tenant.mq.TenantMqProducerInterceptor");
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * 配置MQ消费者租户上下文切面（仅当Spring AMQP存在时）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.amqp.rabbit.annotation.RabbitListener")
    public Object tenantMqConsumerAspect() throws Exception {
        Class<?> clazz = Class.forName("com.psi.common.tenant.mq.TenantMqConsumerAspect");
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * 配置异步方法租户上下文切面（仅当@Async注解存在时）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.scheduling.annotation.Async")
    public Object tenantAsyncAspect() throws Exception {
        Class<?> clazz = Class.forName("com.psi.common.tenant.async.TenantAsyncAspect");
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * 配置Feign租户上下文拦截器（仅当Feign存在时）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "feign.RequestInterceptor")
    public Object tenantFeignInterceptor() throws Exception {
        Class<?> clazz = Class.forName("com.psi.common.tenant.feign.TenantFeignInterceptor");
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * 配置Feign调用上下文清理切面（仅当Feign存在时）
     * 确保Feign调用后清理发起方上下文，解决下游失败的情况
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClient")
    public Object tenantFeignAspect() throws Exception {
        Class<?> clazz = Class.forName("com.psi.common.tenant.feign.TenantFeignAspect");
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * 配置定时任务租户上下文切面（仅当@Scheduled注解存在时）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.scheduling.annotation.Scheduled")
    public Object tenantScheduledAspect() throws Exception {
        Class<?> clazz = Class.forName("com.psi.common.tenant.scheduled.TenantScheduledAspect");
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * 配置完成日志
     */
    @Bean
    public String tenantConfigurationMarker() {
        log.info("Tenant context auto-configuration enabled. " +
                "Required: {}, DefaultTenantId: {}", 
                properties.isRequired(), properties.getDefaultTenantId());
        return "tenant-configuration-enabled";
    }
}