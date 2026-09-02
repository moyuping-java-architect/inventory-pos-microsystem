package com.psi.app.config;

import com.psi.common.mq.template.TtlRabbitTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * MQ 替代配置
 * 在单体微服务架构下，MQ 由虚拟线程替代，不使用真正的 RabbitMQ
 * 但部分组件（如 MqMessageFacade、CashierSyncConsumer）仍依赖 RabbitTemplate 和 TtlRabbitTemplate，
 * 因此提供 stub 实现，保证应用正常启动
 *
 * @author PSI
 */
@Slf4j
@Configuration
public class MQStubConfig {

    /**
     * 提供 ConnectionFactory 的 stub 实现（不真正连接 RabbitMQ）
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ConnectionFactory connectionFactoryStub() {
        log.info("提供 ConnectionFactory 的 stub 实现（单体模式，MQ 由虚拟线程替代）");
        CachingConnectionFactory factory = new CachingConnectionFactory();
        // 不设置 host，避免尝试连接
        // 注意：所有发送消息的方法都会被 stub 覆盖，不会真正发送
        return factory;
    }

    /**
     * 提供 TtlRabbitTemplate 的 stub 实现
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public TtlRabbitTemplate ttlRabbitTemplateStub() {
        log.info("提供 TtlRabbitTemplate 的 stub 实现（单体模式，MQ 由虚拟线程替代）");
        return new StubTtlRabbitTemplate();
    }

    /**
     * 提供 RabbitTemplate 的 stub 实现
     * 显式注入 ConnectionFactory，避免应用自动配置尝试连接 RabbitMQ
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RabbitTemplate rabbitTemplateStub(ConnectionFactory connectionFactoryStub) {
        log.info("提供 RabbitTemplate 的 stub 实现（单体模式，MQ 由虚拟线程替代）");
        return new RabbitTemplate(connectionFactoryStub);
    }

    /**
     * Stub 实现 - 调用 super(null, null)
     * Lombok @RequiredArgsConstructor 会生成 (RabbitTemplate, MQProperties) 构造器
     */
    public static class StubTtlRabbitTemplate extends TtlRabbitTemplate {

        public StubTtlRabbitTemplate() {
            // 调用父类构造器，传入 null（实际不会被使用）
            super(null, null);
        }

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            log.debug("[Stub] convertAndSend: exchange={}, routingKey={}", exchange, routingKey);
        }

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message, MessagePostProcessor postProcessor) {
            log.debug("[Stub] convertAndSend with postProcessor: exchange={}, routingKey={}", exchange, routingKey);
        }
    }
}
