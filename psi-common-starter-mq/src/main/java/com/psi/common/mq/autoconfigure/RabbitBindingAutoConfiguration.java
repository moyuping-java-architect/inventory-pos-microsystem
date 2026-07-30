package com.psi.common.mq.autoconfigure;

import com.psi.common.constant.RabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * RabbitMQ 队列绑定自动配置类
 * 
 * <p>负责初始化以下组件：
 * <ul>
 *   <li>公共交换机和队列</li>
 *   <li>死信交换机和队列</li>
 *   <li>消息记录交换机和队列（用于消息落地持久化）</li>
 *   <li>各组件之间的绑定关系</li>
 * </ul>
 * 
 * <p>配置顺序：在 RabbitAutoConfiguration 之后加载
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@AutoConfiguration(after = RabbitAutoConfiguration.class)
public class RabbitBindingAutoConfiguration {

    /**
     * 声明公共交换机
     * 
     * @return Exchange 公共交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange commonExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.COMMON_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明公共交换机: {}", RabbitMQConstant.COMMON_EXCHANGE);
        return exchange;
    }

    /**
     * 声明公共队列
     * 
     * <p>配置了死信转发：
     * <ul>
     *   <li>死信交换机：{@link RabbitMQConstant#COMMON_DLX_EXCHANGE}</li>
     *   <li>死信路由键：{@link RabbitMQConstant#COMMON_DLX_ROUTING_KEY}</li>
     * </ul>
     * 
     * @return Queue 公共队列实例（持久化）
     */
    @Bean
    public Queue commonQueue() {
        return QueueBuilder.durable(RabbitMQConstant.COMMON_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.COMMON_DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定公共队列到公共交换机
     * 
     * @param commonQueue    公共队列
     * @param commonExchange 公共交换机
     * @return Binding 绑定关系实例
     */
    @Bean
    public Binding commonBinding(Queue commonQueue, Exchange commonExchange) {
        Binding binding = BindingBuilder.bind(commonQueue)
                .to(commonExchange)
                .with(RabbitMQConstant.COMMON_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.COMMON_EXCHANGE,
                RabbitMQConstant.COMMON_QUEUE,
                RabbitMQConstant.COMMON_ROUTING_KEY);
        return binding;
    }

    /**
     * 声明公共死信交换机
     * 
     * @return Exchange 死信交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange commonDeadLetterExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明公共死信交换机: {}", RabbitMQConstant.COMMON_DLX_EXCHANGE);
        return exchange;
    }

    /**
     * 声明公共死信队列
     * 
     * <p>用于存储处理失败的消息，便于后续人工排查和重试
     * 
     * @return Queue 死信队列实例（持久化）
     */
    @Bean
    public Queue commonDeadLetterQueue() {
        Queue queue = QueueBuilder.durable(RabbitMQConstant.COMMON_DLX_QUEUE).build();
        log.info("声明公共死信队列: {}", RabbitMQConstant.COMMON_DLX_QUEUE);
        return queue;
    }

    /**
     * 绑定公共死信队列到死信交换机
     * 
     * @param commonDeadLetterQueue    死信队列
     * @param commonDeadLetterExchange 死信交换机
     * @return Binding 绑定关系实例
     */
    @Bean
    public Binding commonDeadLetterBinding(Queue commonDeadLetterQueue, Exchange commonDeadLetterExchange) {
        Binding binding = BindingBuilder.bind(commonDeadLetterQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.COMMON_DLX_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.COMMON_DLX_EXCHANGE,
                RabbitMQConstant.COMMON_DLX_QUEUE,
                RabbitMQConstant.COMMON_DLX_ROUTING_KEY);
        return binding;
    }

    // ==================== 按业务拆分的死信队列 ====================

    @Bean
    public Queue deadLetterPurchaseQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_PURCHASE_QUEUE).build();
    }

    @Bean
    public Binding deadLetterPurchaseBinding(Queue deadLetterPurchaseQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterPurchaseQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_PURCHASE_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterSaleQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_SALE_QUEUE).build();
    }

    @Bean
    public Binding deadLetterSaleBinding(Queue deadLetterSaleQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterSaleQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_STOCK_QUEUE).build();
    }

    @Bean
    public Binding deadLetterStockBinding(Queue deadLetterStockQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterStockQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_STOCK_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterGoodsQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_GOODS_QUEUE).build();
    }

    @Bean
    public Binding deadLetterGoodsBinding(Queue deadLetterGoodsQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterGoodsQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_GOODS_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterCommonQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_COMMON_QUEUE).build();
    }

    @Bean
    public Binding deadLetterCommonBinding(Queue deadLetterCommonQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterCommonQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_COMMON_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterMessageRecordQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_MESSAGE_RECORD_QUEUE).build();
    }

    @Bean
    public Binding deadLetterMessageRecordBinding(Queue deadLetterMessageRecordQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterMessageRecordQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_MESSAGE_RECORD_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterLoginLogQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_LOGIN_LOG_QUEUE).build();
    }

    @Bean
    public Binding deadLetterLoginLogBinding(Queue deadLetterLoginLogQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterLoginLogQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_LOGIN_LOG_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterFinanceQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_FINANCE_QUEUE).build();
    }

    @Bean
    public Binding deadLetterFinanceBinding(Queue deadLetterFinanceQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterFinanceQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_FINANCE_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue deadLetterSyncQueue() {
        return QueueBuilder.durable(RabbitMQConstant.DLX_SYNC_QUEUE).build();
    }

    @Bean
    public Binding deadLetterSyncBinding(Queue deadLetterSyncQueue, Exchange commonDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterSyncQueue)
                .to(commonDeadLetterExchange)
                .with(RabbitMQConstant.DLX_SYNC_ROUTING_KEY)
                .noargs();
    }

    /**
     * 声明消息记录交换机
     * 
     * <p>用于消息落地持久化，确保消息可追溯和审计
     * 
     * @return Exchange 消息记录交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange messageRecordExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.MESSAGE_RECORD_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明消息记录交换机: {}", RabbitMQConstant.MESSAGE_RECORD_EXCHANGE);
        return exchange;
    }

    /**
     * 声明消息记录队列
     * 
     * <p>用于存储消息记录，支持死信转发：
     * <ul>
     *   <li>死信交换机：{@link RabbitMQConstant#COMMON_DLX_EXCHANGE}</li>
     *   <li>死信路由键：{@link RabbitMQConstant#DLX_MESSAGE_RECORD_ROUTING_KEY}</li>
     * </ul>
     * 
     * @return Queue 消息记录队列实例（持久化）
     */
    @Bean
    public Queue messageRecordQueue() {
        return QueueBuilder.durable(RabbitMQConstant.MESSAGE_RECORD_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_MESSAGE_RECORD_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定消息记录队列到消息记录交换机
     * 
     * @param messageRecordQueue    消息记录队列
     * @param messageRecordExchange 消息记录交换机
     * @return Binding 绑定关系实例
     */
    @Bean
    public Binding messageRecordBinding(Queue messageRecordQueue, Exchange messageRecordExchange) {
        Binding binding = BindingBuilder.bind(messageRecordQueue)
                .to(messageRecordExchange)
                .with(RabbitMQConstant.MESSAGE_RECORD_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.MESSAGE_RECORD_EXCHANGE,
                RabbitMQConstant.MESSAGE_RECORD_QUEUE,
                RabbitMQConstant.MESSAGE_RECORD_ROUTING_KEY);
        return binding;
    }

    // ==================== 登录日志配置 ====================

    /**
     * 声明登录日志交换机
     * 
     * @return Exchange 登录日志交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange loginLogExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.LOGIN_LOG_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明登录日志交换机: {}", RabbitMQConstant.LOGIN_LOG_EXCHANGE);
        return exchange;
    }

    /**
     * 声明登录日志队列
     * 
     * <p>用于存储登录日志消息，支持死信转发：
     * <ul>
     *   <li>死信交换机：{@link RabbitMQConstant#COMMON_DLX_EXCHANGE}</li>
     *   <li>死信路由键：{@link RabbitMQConstant#DLX_LOGIN_LOG_ROUTING_KEY}</li>
     * </ul>
     * 
     * @return Queue 登录日志队列实例（持久化）
     */
    @Bean
    public Queue loginLogQueue() {
        return QueueBuilder.durable(RabbitMQConstant.LOGIN_LOG_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_LOGIN_LOG_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定登录日志队列到登录日志交换机
     * 
     * @param loginLogQueue    登录日志队列
     * @param loginLogExchange 登录日志交换机
     * @return Binding 绑定关系实例
     */
    @Bean
    public Binding loginLogBinding(Queue loginLogQueue, Exchange loginLogExchange) {
        Binding binding = BindingBuilder.bind(loginLogQueue)
                .to(loginLogExchange)
                .with(RabbitMQConstant.LOGIN_LOG_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.LOGIN_LOG_EXCHANGE,
                RabbitMQConstant.LOGIN_LOG_QUEUE,
                RabbitMQConstant.LOGIN_LOG_ROUTING_KEY);
        return binding;
    }

    // ==================== 流程完成通知队列配置 ====================

    /**
     * 声明订单流程完成交换机
     */
    @Bean
    public Exchange processCompletedOrderExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_ORDER_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明流程完成交换机(订单): {}", RabbitMQConstant.PROCESS_COMPLETED_ORDER_EXCHANGE);
        return exchange;
    }

    /**
     * 声明订单流程完成队列
     */
    @Bean
    public Queue processCompletedOrderQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_ORDER_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_PURCHASE_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定订单流程完成队列
     */
    @Bean
    public Binding processCompletedOrderBinding(Queue processCompletedOrderQueue, Exchange processCompletedOrderExchange) {
        Binding binding = BindingBuilder.bind(processCompletedOrderQueue)
                .to(processCompletedOrderExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_ORDER_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.PROCESS_COMPLETED_ORDER_EXCHANGE,
                RabbitMQConstant.PROCESS_COMPLETED_ORDER_QUEUE,
                RabbitMQConstant.PROCESS_COMPLETED_ORDER_ROUTING_KEY);
        return binding;
    }

    /**
     * 声明商品流程完成交换机
     */
    @Bean
    public Exchange processCompletedGoodsExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_GOODS_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明流程完成交换机(商品): {}", RabbitMQConstant.PROCESS_COMPLETED_GOODS_EXCHANGE);
        return exchange;
    }

    /**
     * 声明商品流程完成队列
     */
    @Bean
    public Queue processCompletedGoodsQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_GOODS_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_GOODS_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定商品流程完成队列
     */
    @Bean
    public Binding processCompletedGoodsBinding(Queue processCompletedGoodsQueue, Exchange processCompletedGoodsExchange) {
        Binding binding = BindingBuilder.bind(processCompletedGoodsQueue)
                .to(processCompletedGoodsExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_GOODS_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.PROCESS_COMPLETED_GOODS_EXCHANGE,
                RabbitMQConstant.PROCESS_COMPLETED_GOODS_QUEUE,
                RabbitMQConstant.PROCESS_COMPLETED_GOODS_ROUTING_KEY);
        return binding;
    }

    /**
     * 声明库存流程完成交换机
     */
    @Bean
    public Exchange processCompletedStockExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_STOCK_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明流程完成交换机(库存): {}", RabbitMQConstant.PROCESS_COMPLETED_STOCK_EXCHANGE);
        return exchange;
    }

    /**
     * 声明库存流程完成队列
     */
    @Bean
    public Queue processCompletedStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定库存流程完成队列
     */
    @Bean
    public Binding processCompletedStockBinding(Queue processCompletedStockQueue, Exchange processCompletedStockExchange) {
        Binding binding = BindingBuilder.bind(processCompletedStockQueue)
                .to(processCompletedStockExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_STOCK_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.PROCESS_COMPLETED_STOCK_EXCHANGE,
                RabbitMQConstant.PROCESS_COMPLETED_STOCK_QUEUE,
                RabbitMQConstant.PROCESS_COMPLETED_STOCK_ROUTING_KEY);
        return binding;
    }

    /**
     * 声明通用流程完成交换机
     */
    @Bean
    public Exchange processCompletedCommonExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_COMMON_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明流程完成交换机(通用): {}", RabbitMQConstant.PROCESS_COMPLETED_COMMON_EXCHANGE);
        return exchange;
    }

    /**
     * 声明通用流程完成队列
     */
    @Bean
    public Queue processCompletedCommonQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_COMMON_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_COMMON_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定通用流程完成队列
     */
    @Bean
    public Binding processCompletedCommonBinding(Queue processCompletedCommonQueue, Exchange processCompletedCommonExchange) {
        Binding binding = BindingBuilder.bind(processCompletedCommonQueue)
                .to(processCompletedCommonExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_COMMON_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.PROCESS_COMPLETED_COMMON_EXCHANGE,
                RabbitMQConstant.PROCESS_COMPLETED_COMMON_QUEUE,
                RabbitMQConstant.PROCESS_COMPLETED_COMMON_ROUTING_KEY);
        return binding;
    }

    // ==================== 单据级流程完成通知配置 ====================

    @Bean
    public Exchange processCompletedPurchaseExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue processCompletedPurchaseQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_PURCHASE_ROUTING_KEY).build();
    }

    @Bean
    public Binding processCompletedPurchaseBinding(Queue processCompletedPurchaseQueue, Exchange processCompletedPurchaseExchange) {
        return BindingBuilder.bind(processCompletedPurchaseQueue).to(processCompletedPurchaseExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_ROUTING_KEY).noargs();
    }

    @Bean
    public Exchange processCompletedSaleExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_SALE_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue processCompletedSaleQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_SALE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY).build();
    }

    @Bean
    public Binding processCompletedSaleBinding(Queue processCompletedSaleQueue, Exchange processCompletedSaleExchange) {
        return BindingBuilder.bind(processCompletedSaleQueue).to(processCompletedSaleExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_SALE_ROUTING_KEY).noargs();
    }

    @Bean
    public Exchange processCompletedLossExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_LOSS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue processCompletedLossQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_LOSS_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding processCompletedLossBinding(Queue processCompletedLossQueue, Exchange processCompletedLossExchange) {
        return BindingBuilder.bind(processCompletedLossQueue).to(processCompletedLossExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_LOSS_ROUTING_KEY).noargs();
    }

    @Bean
    public Exchange processCompletedOverflowExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_OVERFLOW_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue processCompletedOverflowQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_OVERFLOW_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding processCompletedOverflowBinding(Queue processCompletedOverflowQueue, Exchange processCompletedOverflowExchange) {
        return BindingBuilder.bind(processCompletedOverflowQueue).to(processCompletedOverflowExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_OVERFLOW_ROUTING_KEY).noargs();
    }

    @Bean
    public Exchange processCompletedCheckExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.PROCESS_COMPLETED_CHECK_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue processCompletedCheckQueue() {
        return QueueBuilder.durable(RabbitMQConstant.PROCESS_COMPLETED_CHECK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_STOCK_ROUTING_KEY).build();
    }

    @Bean
    public Binding processCompletedCheckBinding(Queue processCompletedCheckQueue, Exchange processCompletedCheckExchange) {
        return BindingBuilder.bind(processCompletedCheckQueue).to(processCompletedCheckExchange)
                .with(RabbitMQConstant.PROCESS_COMPLETED_CHECK_ROUTING_KEY).noargs();
    }

    // ==================== 销售业务队列配置 ====================

    // ==================== 收银业务 ====================

    /**
     * 声明收银业务交换机
     * 
     * @return Exchange 收银交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange cashierExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.CASHIER_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明收银业务交换机: {}", RabbitMQConstant.CASHIER_EXCHANGE);
        return exchange;
    }

    /**
     * 声明收银财务队列
     * 
     * <p>用于接收收银消息，由财务模块消费处理：
     * <ul>
     *   <li>记录销售日报</li>
     *   <li>记录收款流水</li>
     * </ul>
     * 
     * @return Queue 收银财务队列实例（持久化）
     */
    @Bean
    public Queue cashierFinanceQueue() {
        return QueueBuilder.durable(RabbitMQConstant.CASHIER_FINANCE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 声明收银库存队列
     * 
     * <p>用于接收收银消息，由库存模块消费处理：
     * <ul>
     *   <li>扣减库存</li>
     *   <li>记录库存流水</li>
     * </ul>
     * 
     * @return Queue 收银库存队列实例（持久化）
     */
    @Bean
    public Queue cashierStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.CASHIER_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定收银财务队列到收银交换机
     */
    @Bean
    public Binding cashierFinanceBinding(Queue cashierFinanceQueue, Exchange cashierExchange) {
        Binding binding = BindingBuilder.bind(cashierFinanceQueue)
                .to(cashierExchange)
                .with(RabbitMQConstant.CASHIER_FINANCE_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.CASHIER_EXCHANGE,
                RabbitMQConstant.CASHIER_FINANCE_QUEUE,
                RabbitMQConstant.CASHIER_FINANCE_ROUTING_KEY);
        return binding;
    }

    /**
     * 绑定收银库存队列到收银交换机
     */
    @Bean
    public Binding cashierStockBinding(Queue cashierStockQueue, Exchange cashierExchange) {
        Binding binding = BindingBuilder.bind(cashierStockQueue)
                .to(cashierExchange)
                .with(RabbitMQConstant.CASHIER_STOCK_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.CASHIER_EXCHANGE,
                RabbitMQConstant.CASHIER_STOCK_QUEUE,
                RabbitMQConstant.CASHIER_STOCK_ROUTING_KEY);
        return binding;
    }

    // ==================== 销售订单业务 ====================

    /**
     * 声明销售订单交换机
     * 
     * @return Exchange 销售订单交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange saleOrderExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.SALE_ORDER_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明销售订单交换机: {}", RabbitMQConstant.SALE_ORDER_EXCHANGE);
        return exchange;
    }

    /**
     * 声明销售订单财务队列
     * 
     * @return Queue 销售订单财务队列实例（持久化）
     */
    @Bean
    public Queue saleOrderFinanceQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SALE_ORDER_FINANCE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 声明销售订单库存队列
     * 
     * @return Queue 销售订单库存队列实例（持久化）
     */
    @Bean
    public Queue saleOrderStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SALE_ORDER_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定销售订单财务队列到销售订单交换机
     */
    @Bean
    public Binding saleOrderFinanceBinding(Queue saleOrderFinanceQueue, Exchange saleOrderExchange) {
        Binding binding = BindingBuilder.bind(saleOrderFinanceQueue)
                .to(saleOrderExchange)
                .with(RabbitMQConstant.SALE_ORDER_FINANCE_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SALE_ORDER_EXCHANGE,
                RabbitMQConstant.SALE_ORDER_FINANCE_QUEUE,
                RabbitMQConstant.SALE_ORDER_FINANCE_ROUTING_KEY);
        return binding;
    }

    /**
     * 绑定销售订单库存队列到销售订单交换机
     */
    @Bean
    public Binding saleOrderStockBinding(Queue saleOrderStockQueue, Exchange saleOrderExchange) {
        Binding binding = BindingBuilder.bind(saleOrderStockQueue)
                .to(saleOrderExchange)
                .with(RabbitMQConstant.SALE_ORDER_STOCK_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SALE_ORDER_EXCHANGE,
                RabbitMQConstant.SALE_ORDER_STOCK_QUEUE,
                RabbitMQConstant.SALE_ORDER_STOCK_ROUTING_KEY);
        return binding;
    }

    /**
     * 声明销售订单释放库存队列
     *
     * @return Queue 销售订单释放库存队列实例（持久化）
     */
    @Bean
    public Queue saleOrderReleaseQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SALE_ORDER_RELEASE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定销售订单释放库存队列到销售订单交换机
     */
    @Bean
    public Binding saleOrderReleaseBinding(Queue saleOrderReleaseQueue, Exchange saleOrderExchange) {
        Binding binding = BindingBuilder.bind(saleOrderReleaseQueue)
                .to(saleOrderExchange)
                .with(RabbitMQConstant.SALE_ORDER_RELEASE_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SALE_ORDER_EXCHANGE,
                RabbitMQConstant.SALE_ORDER_RELEASE_QUEUE,
                RabbitMQConstant.SALE_ORDER_RELEASE_ROUTING_KEY);
        return binding;
    }

    // ==================== 销售出库业务 ====================

    /**
     * 声明销售出库交换机
     * 
     * @return Exchange 销售出库交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange saleOutExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.SALE_OUT_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明销售出库交换机: {}", RabbitMQConstant.SALE_OUT_EXCHANGE);
        return exchange;
    }

    /**
     * 声明销售出库财务队列
     * 
     * @return Queue 销售出库财务队列实例（持久化）
     */
    @Bean
    public Queue saleOutFinanceQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SALE_OUT_FINANCE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 声明销售出库库存队列
     * 
     * @return Queue 销售出库库存队列实例（持久化）
     */
    @Bean
    public Queue saleOutStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SALE_OUT_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定销售出库财务队列到销售出库交换机
     */
    @Bean
    public Binding saleOutFinanceBinding(Queue saleOutFinanceQueue, Exchange saleOutExchange) {
        Binding binding = BindingBuilder.bind(saleOutFinanceQueue)
                .to(saleOutExchange)
                .with(RabbitMQConstant.SALE_OUT_FINANCE_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SALE_OUT_EXCHANGE,
                RabbitMQConstant.SALE_OUT_FINANCE_QUEUE,
                RabbitMQConstant.SALE_OUT_FINANCE_ROUTING_KEY);
        return binding;
    }

    /**
     * 绑定销售出库库存队列到销售出库交换机
     */
    @Bean
    public Binding saleOutStockBinding(Queue saleOutStockQueue, Exchange saleOutExchange) {
        Binding binding = BindingBuilder.bind(saleOutStockQueue)
                .to(saleOutExchange)
                .with(RabbitMQConstant.SALE_OUT_STOCK_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SALE_OUT_EXCHANGE,
                RabbitMQConstant.SALE_OUT_STOCK_QUEUE,
                RabbitMQConstant.SALE_OUT_STOCK_ROUTING_KEY);
        return binding;
    }

    // ==================== 销售退货业务 ====================

    /**
     * 声明销售退货交换机
     * 
     * @return Exchange 销售退货交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange saleReturnExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.SALE_RETURN_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明销售退货交换机: {}", RabbitMQConstant.SALE_RETURN_EXCHANGE);
        return exchange;
    }

    /**
     * 声明销售退货财务队列
     * 
     * @return Queue 销售退货财务队列实例（持久化）
     */
    @Bean
    public Queue saleReturnFinanceQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SALE_RETURN_FINANCE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 声明销售退货库存队列
     * 
     * @return Queue 销售退货库存队列实例（持久化）
     */
    @Bean
    public Queue saleReturnStockQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SALE_RETURN_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SALE_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定销售退货财务队列到销售退货交换机
     */
    @Bean
    public Binding saleReturnFinanceBinding(Queue saleReturnFinanceQueue, Exchange saleReturnExchange) {
        Binding binding = BindingBuilder.bind(saleReturnFinanceQueue)
                .to(saleReturnExchange)
                .with(RabbitMQConstant.SALE_RETURN_FINANCE_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SALE_RETURN_EXCHANGE,
                RabbitMQConstant.SALE_RETURN_FINANCE_QUEUE,
                RabbitMQConstant.SALE_RETURN_FINANCE_ROUTING_KEY);
        return binding;
    }

    /**
     * 绑定销售退货库存队列到销售退货交换机
     */
    @Bean
    public Binding saleReturnStockBinding(Queue saleReturnStockQueue, Exchange saleReturnExchange) {
        Binding binding = BindingBuilder.bind(saleReturnStockQueue)
                .to(saleReturnExchange)
                .with(RabbitMQConstant.SALE_RETURN_STOCK_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SALE_RETURN_EXCHANGE,
                RabbitMQConstant.SALE_RETURN_STOCK_QUEUE,
                RabbitMQConstant.SALE_RETURN_STOCK_ROUTING_KEY);
        return binding;
    }

    // ==================== 数据同步队列配置 ====================

    /**
     * 声明上行同步交换机（POS → sync-ms）
     * 
     * @return Exchange 上行同步交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange syncUpExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.SYNC_UP_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明上行同步交换机: {}", RabbitMQConstant.SYNC_UP_EXCHANGE);
        return exchange;
    }

    /**
     * 声明上行同步队列（POS → sync-ms）
     * 
     * <p>用于接收POS发送的同步数据，由sync-ms消费处理
     * 
     * @return Queue 上行同步队列实例（持久化）
     */
    @Bean
    public Queue syncUpQueue() {
        return QueueBuilder.durable(RabbitMQConstant.SYNC_UP_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SYNC_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定上行同步队列到上行同步交换机
     */
    @Bean
    public Binding syncUpBinding(Queue syncUpQueue, Exchange syncUpExchange) {
        Binding binding = BindingBuilder.bind(syncUpQueue)
                .to(syncUpExchange)
                .with(RabbitMQConstant.SYNC_UP_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                RabbitMQConstant.SYNC_UP_QUEUE,
                RabbitMQConstant.SYNC_UP_ROUTING_KEY);
        return binding;
    }

    // ==================== 商品数据同步队列配置 ====================

    /**
     * 声明商品上行同步队列（goods → sync-ms）
     * 
     * <p>用于接收商品微服务发送的商品同步数据，由sync-ms消费处理
     * 
     * @return Queue 商品上行同步队列实例（持久化）
     */
    @Bean
    public Queue syncUpGoodsQueue() {
        Queue queue = QueueBuilder.durable(RabbitMQConstant.SYNC_UP_GOODS_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SYNC_ROUTING_KEY)
                .build();
        log.info("声明商品上行同步队列: {}", RabbitMQConstant.SYNC_UP_GOODS_QUEUE);
        return queue;
    }

    /**
     * 绑定商品上行同步队列到上行同步交换机
     */
    @Bean
    public Binding syncUpGoodsBinding(Queue syncUpGoodsQueue, Exchange syncUpExchange) {
        Binding binding = BindingBuilder.bind(syncUpGoodsQueue)
                .to(syncUpExchange)
                .with(RabbitMQConstant.SYNC_UP_GOODS_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                RabbitMQConstant.SYNC_UP_GOODS_QUEUE,
                RabbitMQConstant.SYNC_UP_GOODS_ROUTING_KEY);
        return binding;
    }

    // ==================== 客户上行同步队列配置 ====================

    /**
     * 声明上行同步客户队列
     */
    @Bean
    public Queue syncUpCustomerQueue() {
        Queue queue = QueueBuilder.durable(RabbitMQConstant.SYNC_UP_CUSTOMER_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SYNC_ROUTING_KEY)
                .build();
        log.info("声明上行同步客户队列: {}", RabbitMQConstant.SYNC_UP_CUSTOMER_QUEUE);
        return queue;
    }

    /**
     * 绑定客户队列到上行同步交换机
     */
    @Bean
    public Binding syncUpCustomerBinding(Queue syncUpCustomerQueue, Exchange syncUpExchange) {
        Binding binding = BindingBuilder.bind(syncUpCustomerQueue)
                .to(syncUpExchange)
                .with(RabbitMQConstant.SYNC_UP_CUSTOMER_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                RabbitMQConstant.SYNC_UP_CUSTOMER_QUEUE,
                RabbitMQConstant.SYNC_UP_CUSTOMER_ROUTING_KEY);
        return binding;
    }

    // ==================== 采购上行同步队列配置 ====================

    /**
     * 声明上行同步采购队列
     */
    @Bean
    public Queue syncUpPurchaseQueue() {
        Queue queue = QueueBuilder.durable(RabbitMQConstant.SYNC_UP_PURCHASE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SYNC_ROUTING_KEY)
                .build();
        log.info("声明上行同步采购队列: {}", RabbitMQConstant.SYNC_UP_PURCHASE_QUEUE);
        return queue;
    }

    /**
     * 绑定采购队列到上行同步交换机
     */
    @Bean
    public Binding syncUpPurchaseBinding(Queue syncUpPurchaseQueue, Exchange syncUpExchange) {
        Binding binding = BindingBuilder.bind(syncUpPurchaseQueue)
                .to(syncUpExchange)
                .with(RabbitMQConstant.SYNC_UP_PURCHASE_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                RabbitMQConstant.SYNC_UP_PURCHASE_QUEUE,
                RabbitMQConstant.SYNC_UP_PURCHASE_ROUTING_KEY);
        return binding;
    }

    // ==================== 库存上行同步队列配置 ====================

    /**
     * 声明上行同步库存队列
     */
    @Bean
    public Queue syncUpStockQueue() {
        Queue queue = QueueBuilder.durable(RabbitMQConstant.SYNC_UP_STOCK_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SYNC_ROUTING_KEY)
                .build();
        log.info("声明上行同步库存队列: {}", RabbitMQConstant.SYNC_UP_STOCK_QUEUE);
        return queue;
    }

    /**
     * 绑定库存队列到上行同步交换机
     */
    @Bean
    public Binding syncUpStockBinding(Queue syncUpStockQueue, Exchange syncUpExchange) {
        Binding binding = BindingBuilder.bind(syncUpStockQueue)
                .to(syncUpExchange)
                .with(RabbitMQConstant.SYNC_UP_STOCK_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                RabbitMQConstant.SYNC_UP_STOCK_QUEUE,
                RabbitMQConstant.SYNC_UP_STOCK_ROUTING_KEY);
        return binding;
    }

    // ==================== 财务上行同步队列配置 ====================

    /**
     * 声明上行同步财务队列
     */
    @Bean
    public Queue syncUpFinanceQueue() {
        Queue queue = QueueBuilder.durable(RabbitMQConstant.SYNC_UP_FINANCE_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SYNC_ROUTING_KEY)
                .build();
        log.info("声明上行同步财务队列: {}", RabbitMQConstant.SYNC_UP_FINANCE_QUEUE);
        return queue;
    }

    /**
     * 绑定财务队列到上行同步交换机
     */
    @Bean
    public Binding syncUpFinanceBinding(Queue syncUpFinanceQueue, Exchange syncUpExchange) {
        Binding binding = BindingBuilder.bind(syncUpFinanceQueue)
                .to(syncUpExchange)
                .with(RabbitMQConstant.SYNC_UP_FINANCE_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SYNC_UP_EXCHANGE,
                RabbitMQConstant.SYNC_UP_FINANCE_QUEUE,
                RabbitMQConstant.SYNC_UP_FINANCE_ROUTING_KEY);
        return binding;
    }

    // ==================== 下行同步队列配置（后台 → POS） ====================

    /**
     * 声明下行同步交换机（后台 → POS）
     *
     * @return Exchange 下行同步交换机实例（持久化 Direct 类型）
     */
    @Bean
    public Exchange syncDownExchange() {
        Exchange exchange = ExchangeBuilder.directExchange(RabbitMQConstant.SYNC_DOWN_EXCHANGE)
                .durable(true)
                .build();
        log.info("声明下行同步交换机: {}", RabbitMQConstant.SYNC_DOWN_EXCHANGE);
        return exchange;
    }

    /**
     * 声明下行同步队列（后台 → POS）
     *
     * <p>用于接收后台系统发送的下行同步数据，写入 down_sync 表由 POS 拉取
     *
     * @return Queue 下行同步队列实例（持久化）
     */
    @Bean
    public Queue syncDownQueue() {
        Queue queue = QueueBuilder.durable(RabbitMQConstant.SYNC_DOWN_QUEUE)
                .deadLetterExchange(RabbitMQConstant.COMMON_DLX_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstant.DLX_SYNC_ROUTING_KEY)
                .build();
        log.info("声明下行同步队列: {}", RabbitMQConstant.SYNC_DOWN_QUEUE);
        return queue;
    }

    /**
     * 绑定下行同步队列到下行同步交换机
     */
    @Bean
    public Binding syncDownBinding(Queue syncDownQueue, Exchange syncDownExchange) {
        Binding binding = BindingBuilder.bind(syncDownQueue)
                .to(syncDownExchange)
                .with(RabbitMQConstant.SYNC_DOWN_ROUTING_KEY)
                .noargs();
        log.info("声明绑定关系: {} -> {} ({})",
                RabbitMQConstant.SYNC_DOWN_EXCHANGE,
                RabbitMQConstant.SYNC_DOWN_QUEUE,
                RabbitMQConstant.SYNC_DOWN_ROUTING_KEY);
        return binding;
    }

}