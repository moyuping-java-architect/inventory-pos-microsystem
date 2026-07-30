package com.psi.goods.config;

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
 * 商品模块MQ队列配置
 */
@Configuration
public class GoodsMqQueueConfig {

    @Bean
    public Exchange goodsProcessCompletedExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_GOODS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue goodsProcessCompletedQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_GOODS_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_GOODS_ROUTING_KEY).build();
    }

    @Bean
    public Binding goodsProcessCompletedBinding(Queue goodsProcessCompletedQueue, Exchange goodsProcessCompletedExchange) {
        return BindingBuilder.bind(goodsProcessCompletedQueue).to(goodsProcessCompletedExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_GOODS_ROUTING_KEY).noargs();
    }
}