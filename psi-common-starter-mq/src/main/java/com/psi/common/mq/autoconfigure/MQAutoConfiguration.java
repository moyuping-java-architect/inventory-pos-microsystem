package com.psi.common.mq.autoconfigure;

import com.psi.common.mq.config.MQProperties;
import com.psi.common.mq.template.TtlRabbitTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MQ 自动配置类
 * 配置 TTL 租户上下文透传相关组件
 * Spring Boot 3.x 自动配置方式
 * 支持多微服务架构下的全局消息拦截
 * 
 * @author PSI
 * @version 2.0.0
 */
@Slf4j
@AutoConfiguration(after = RabbitAutoConfiguration.class)
@EnableConfigurationProperties(MQProperties.class)
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "psi.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class MQAutoConfiguration {

    private final MQProperties properties;

    /**
     * 配置 JSON 消息转换器（由 Spring AMQP 自动使用）
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 TTL 消息发送模板
     * 
     * @param rabbitTemplate RabbitMQ 模板（由 RabbitAutoConfiguration 提供）
     * @return TtlRabbitTemplate 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(RabbitTemplate.class)
    public TtlRabbitTemplate ttlRabbitTemplate(RabbitTemplate rabbitTemplate) {
        log.info("TtlRabbitTemplate configured");
        return new TtlRabbitTemplate(rabbitTemplate, properties);
    }
}