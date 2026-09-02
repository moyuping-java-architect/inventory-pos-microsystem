package com.psi.stock.config;

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
 * 库存模块MQ队列配置
 */
@Configuration
public class StockMqQueueConfig {

    @Bean
    public Exchange stockProcessCompletedLossExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_LOSS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue stockProcessCompletedLossQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_LOSS_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding stockProcessCompletedLossBinding(Queue stockProcessCompletedLossQueue, Exchange stockProcessCompletedLossExchange) {
        return BindingBuilder.bind(stockProcessCompletedLossQueue).to(stockProcessCompletedLossExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_LOSS_ROUTING_KEY).noargs();
    }

    @Bean
    public Exchange stockProcessCompletedOverflowExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_OVERFLOW_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue stockProcessCompletedOverflowQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_OVERFLOW_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding stockProcessCompletedOverflowBinding(Queue stockProcessCompletedOverflowQueue, Exchange stockProcessCompletedOverflowExchange) {
        return BindingBuilder.bind(stockProcessCompletedOverflowQueue).to(stockProcessCompletedOverflowExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_OVERFLOW_ROUTING_KEY).noargs();
    }

    @Bean
    public Exchange stockProcessCompletedCheckExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_CHECK_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue stockProcessCompletedCheckQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_CHECK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding stockProcessCompletedCheckBinding(Queue stockProcessCompletedCheckQueue, Exchange stockProcessCompletedCheckExchange) {
        return BindingBuilder.bind(stockProcessCompletedCheckQueue).to(stockProcessCompletedCheckExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_CHECK_ROUTING_KEY).noargs();
    }

    @Bean
    public Exchange stockProcessCompletedStockExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_STOCK_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue stockProcessCompletedStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding stockProcessCompletedStockBinding(Queue stockProcessCompletedStockQueue, Exchange stockProcessCompletedStockExchange) {
        return BindingBuilder.bind(stockProcessCompletedStockQueue).to(stockProcessCompletedStockExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_STOCK_ROUTING_KEY).noargs();
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

    // ==================== 销售退货库存配置 ====================
    // 队列和绑定在 RabbitBindingAutoConfiguration 中已定义
}