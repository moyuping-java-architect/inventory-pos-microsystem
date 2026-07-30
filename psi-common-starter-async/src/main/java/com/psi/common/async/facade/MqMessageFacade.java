package com.psi.common.async.facade;

import com.psi.common.config.AppGlobalConfig;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.context.UserContext;
import com.psi.common.context.VirtualThreadContextWrapper;
import com.psi.common.exception.MqSendException;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.mq.template.TtlRabbitTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;



/**
 * MQ消息发送门面
 * 提供简洁、统一的MQ消息发送API
 * 
 * <p>公共方法：
 * <ul>
 *   <li>{@link #send(MqCommonMessage)} - 同步发送</li>
 *   <li>{@link #sendAsync(MqCommonMessage)} - 异步发送</li>
 *   <li>{@link #sendWithDelay(MqCommonMessage, Duration)} - 同步延迟发送</li>
 *   <li>{@link #sendAsyncWithDelay(MqCommonMessage, Duration)} - 异步延迟发送</li>
 * </ul>
 * 
 * @author PSI
 * @version 4.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqMessageFacade {

    private final TtlRabbitTemplate ttlRabbitTemplate;


    // ==================== 公共方法 ====================

    /**
     * 同步发送消息
     * 
     * @param message MqCommonMessage消息对象
     */
    public void send(MqCommonMessage<?> message) {
        prepareMessage(message);
        doSend(message);
    }

    /**
     * 异步发送消息
     * 
     * @param message MqCommonMessage消息对象
     */
    public void sendAsync(MqCommonMessage<?> message) {
        executeAsync(() -> send(message));
    }

    /**
     * 同步发送延迟消息
     * 
     * @param message MqCommonMessage消息对象
     * @param delay   延迟时间
     */
    public void sendWithDelay(MqCommonMessage<?> message, Duration delay) {
        prepareMessage(message);
        long delayMillis = delay.toMillis();
        message.getExtParams().put("delayMillis", String.valueOf(delayMillis));
        doSendWithDelay(message, delayMillis);
    }

    /**
     * 异步发送延迟消息
     * 
     * @param message MqCommonMessage消息对象
     * @param delay   延迟时间
     */
    public void sendAsyncWithDelay(MqCommonMessage<?> message, Duration delay) {
        executeAsync(() -> sendWithDelay(message, delay));
    }

    // ==================== 私有方法 ====================

    private void executeAsync(Runnable task) {
        VirtualThreadContextWrapper.executeAsync(task);
    }

    private void prepareMessage(MqCommonMessage<?> message) {
        if (message == null) {
            throw new IllegalArgumentException("MqCommonMessage cannot be null");
        }
        if (!StringUtils.hasText(message.getMessageId())) {
            message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (!StringUtils.hasText(message.getSourceService())) {
            message.setSourceService(AppGlobalConfig.getCurrentServiceName());
        }
        if (!StringUtils.hasText(message.getTenantId())) {
            message.setTenantId(UserContext.getTenantId());
        }
        if (!StringUtils.hasText(message.getOperatorId())) {
            message.setOperatorId(UserContext.getUserId());
        }
        if (message.getCreateTime() == null) {
            message.setCreateTime(System.currentTimeMillis());
        }
        if (message.getExtParams() == null) {
            message.setExtParams(new HashMap<>());
        }
        if (!StringUtils.hasText(message.getExchangeName())) {
            throw new IllegalArgumentException("Exchange name cannot be empty");
        }
        if (!StringUtils.hasText(message.getRoutingKey())) {
            throw new IllegalArgumentException("Routing key cannot be empty");
        }
    }

    private void doSend(MqCommonMessage<?> message) {
        try {
            sendMessageRecordAsync(message);
            ttlRabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(), message);
            log.debug("MQ message sent: exchange={}, routingKey={}, messageId={}", 
                    message.getExchangeName(), message.getRoutingKey(), message.getMessageId());
        } catch (Exception e) {
            log.error("Failed to send MQ message: exchange={}, routingKey={}, messageId={}", 
                    message.getExchangeName(), message.getRoutingKey(), message.getMessageId(), e);
            throw new MqSendException("Failed to send MQ message", e);
        }
    }

    private void doSendWithDelay(MqCommonMessage<?> message, long delayMillis) {
        try {
            sendMessageRecordAsync(message);
            ttlRabbitTemplate.convertAndSendWithDelay(message.getExchangeName(), 
                    message.getRoutingKey(), message, delayMillis);
            log.debug("Delayed MQ message sent: exchange={}, routingKey={}, delay={}ms, messageId={}", 
                    message.getExchangeName(), message.getRoutingKey(), delayMillis, message.getMessageId());
        } catch (Exception e) {
            log.error("Failed to send delayed MQ message: exchange={}, routingKey={}", 
                    message.getExchangeName(), message.getRoutingKey(), e);
            throw new MqSendException("Failed to send delayed MQ message", e);
        }
    }

    private void sendMessageRecordAsync(MqCommonMessage<?> message) {
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                ttlRabbitTemplate.convertAndSend(RabbitMQConstant.MESSAGE_RECORD_EXCHANGE, RabbitMQConstant.MESSAGE_RECORD_ROUTING_KEY, message);
                log.debug("Message record sent: messageId={}", message.getMessageId());
            } catch (Exception e) {
                log.error("Failed to send message record: messageId={}", message.getMessageId(), e);
            }
        });
    }
}