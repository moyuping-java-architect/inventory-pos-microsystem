package com.psi.purchase.config;

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
 * 采购模块MQ队列配置
 */
@Configuration
public class PurchaseMqQueueConfig {

    @Bean
    public Exchange purchaseProcessCompletedExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue purchaseProcessCompletedQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_PURCHASE_ROUTING_KEY).build();
    }

    @Bean
    public Binding purchaseProcessCompletedBinding(Queue purchaseProcessCompletedQueue, Exchange purchaseProcessCompletedExchange) {
        return BindingBuilder.bind(purchaseProcessCompletedQueue).to(purchaseProcessCompletedExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_ROUTING_KEY).noargs();
    }
    // ==================== 采购入库库存配置 ====================

    @Bean
    public Exchange purchaseInStockExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PURCHASE_IN_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue purchaseInStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PURCHASE_IN_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding purchaseInStockBinding(Queue purchaseInStockQueue, Exchange purchaseInStockExchange) {
        return BindingBuilder.bind(purchaseInStockQueue).to(purchaseInStockExchange)
                .with(RabbitMQConstant.PURCHASE_IN_STOCK_ROUTING_KEY).noargs();
    }

    // ==================== 采购退货库存配置 ====================

    @Bean
    public Exchange purchaseReturnStockExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PURCHASE_RETURN_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue purchaseReturnStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PURCHASE_RETURN_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding purchaseReturnStockBinding(Queue purchaseReturnStockQueue, Exchange purchaseReturnStockExchange) {
        return BindingBuilder.bind(purchaseReturnStockQueue).to(purchaseReturnStockExchange)
                .with(RabbitMQConstant.PURCHASE_RETURN_STOCK_ROUTING_KEY).noargs();
    }
}