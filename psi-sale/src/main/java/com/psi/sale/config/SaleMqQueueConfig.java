package com.psi.sale.config;

import com.psi.common.constant.RabbitMQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 销售模块MQ队列配置
 */
@Configuration
public class SaleMqQueueConfig {

    @Bean
    public Exchange saleProcessCompletedExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_SALE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue saleProcessCompletedQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_SALE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY).build();
    }

    @Bean
    public Binding saleProcessCompletedBinding(Queue saleProcessCompletedQueue, Exchange saleProcessCompletedExchange) {
        return BindingBuilder.bind(saleProcessCompletedQueue).to(saleProcessCompletedExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_SALE_ROUTING_KEY).noargs();
    }

    // ==================== 销售出库库存配置 ====================
    // 队列和绑定在 RabbitBindingAutoConfiguration 中已定义

    // ==================== 销售退货库存配置 ====================
    // 队列和绑定在 RabbitBindingAutoConfiguration 中已定义
}