package com.psi.common.mq.template;

import com.alibaba.ttl.TtlRunnable;
import com.psi.common.mq.config.MQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * TTL 消息发送模板
 * 
 * <p>TTL = TransmittableThreadLocal（阿里巴巴）
 * 在线程池/异步场景下，普通 ThreadLocal 的值无法传递到子线程
 * TTL 通过包装 Runnable/Callable，实现上下文在线程间的透传
 * 
 * <p>核心功能：
 * <ul>
 *   <li>使用 TtlRunnable 包装发送任务，确保 UserContext 中的租户信息传递</li>
 *   <li>支持延迟消息（x-delay）</li>
 *   <li>租户上下文透传由 TenantMqProducerInterceptor 统一处理</li>
 * </ul>
 * 
 * <p>与 TenantMqProducerInterceptor 的配合：
 * <ol>
 *   <li>此模板负责 TTL 线程上下文透传</li>
 *   <li>TenantMqProducerInterceptor 负责将上下文写入消息头</li>
 *   <li>两者配合实现完整的跨服务租户透传</li>
 * </ol>
 * 
 * @author PSI
 * @version 2.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TtlRabbitTemplate {

    private final RabbitTemplate rabbitTemplate;
    private final MQProperties properties;

    /**
     * 发送消息到指定交换机
     * 
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     */
    public void convertAndSend(String exchange, String routingKey, Object message) {
        runWithTtl(() -> rabbitTemplate.convertAndSend(exchange, routingKey, message));
        log.debug("Message sent: exchange={}, routingKey={}", exchange, routingKey);
    }

    /**
     * 发送消息到指定交换机（带消息处理器）
     * 
     * @param exchange        交换机名称
     * @param routingKey      路由键
     * @param message         消息内容
     * @param postProcessor   消息处理器
     */
    public void convertAndSend(String exchange, String routingKey, Object message, 
                               MessagePostProcessor postProcessor) {
        runWithTtl(() -> rabbitTemplate.convertAndSend(exchange, routingKey, message, postProcessor));
        log.debug("Message sent with postProcessor: exchange={}, routingKey={}", exchange, routingKey);
    }

    /**
     * 发送延迟消息到指定交换机
     * 
     * @param exchange     交换机名称
     * @param routingKey   路由键
     * @param message      消息内容
     * @param delayMillis  延迟时间（毫秒）
     */
    public void convertAndSendWithDelay(String exchange, String routingKey, Object message, long delayMillis) {
        runWithTtl(() -> {
            MessagePostProcessor delayProcessor = msg -> {
                msg.getMessageProperties().getHeaders().put("x-delay", delayMillis);
                return msg;
            };
            rabbitTemplate.convertAndSend(exchange, routingKey, message, delayProcessor);
        });
        log.debug("Delayed message sent: exchange={}, routingKey={}, delay={}ms", 
                exchange, routingKey, delayMillis);
    }

    /**
     * 发送消息到指定队列（简化版，使用空交换机）
     * 
     * @param queue    队列名称
     * @param message  消息内容
     */
    public void convertAndSend(String queue, Object message) {
        convertAndSend("", queue, message);
    }

    /**
     * 发送延迟消息到指定队列（简化版）
     * 
     * @param queue       队列名称
     * @param message     消息内容
     * @param delayMillis 延迟时间（毫秒）
     */
    public void convertAndSendWithDelay(String queue, Object message, long delayMillis) {
        convertAndSendWithDelay("", queue, message, delayMillis);
    }

    /**
     * 使用 TTL 包装执行任务
     * 
     * <p>确保 UserContext 在异步线程中正确传递
     */
    private void runWithTtl(Runnable task) {
        try {
            TtlRunnable.get(task).run();
        } catch (Exception e) {
            log.error("Failed to send message", e);
            throw e;
        }
    }

    // ==================== Getter/Setter ====================

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }

    public void setMessageConverter(MessageConverter messageConverter) {
        rabbitTemplate.setMessageConverter(messageConverter);
    }
}