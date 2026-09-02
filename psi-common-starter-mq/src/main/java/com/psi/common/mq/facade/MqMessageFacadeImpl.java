package com.psi.common.mq.facade;

import com.psi.common.message.MqMessageFacade;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.mq.template.TtlRabbitTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(name = "psi.mq.enabled", havingValue = "true", matchIfMissing = true)
public class MqMessageFacadeImpl implements MqMessageFacade {

    private final TtlRabbitTemplate ttlRabbitTemplate;

    public MqMessageFacadeImpl(TtlRabbitTemplate ttlRabbitTemplate) {
        this.ttlRabbitTemplate = ttlRabbitTemplate;
    }

    @Override
    public void send(MqCommonMessage<?> message) {
        if (message == null) {
            return;
        }
        try {
            ttlRabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(), message);
        } catch (Exception e) {
            log.error("MQ send failed: exchange={}, routingKey={}", message.getExchangeName(), message.getRoutingKey(), e);
        }
    }

    @Override
    public void sendAsync(MqCommonMessage<?> message) {
        if (message == null) {
            return;
        }
        try {
            ttlRabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(), message);
        } catch (Exception e) {
            log.error("MQ sendAsync failed: exchange={}, routingKey={}", message.getExchangeName(), message.getRoutingKey(), e);
        }
    }

    @Override
    public void sendWithDelay(MqCommonMessage<?> message, Duration delay) {
        if (message == null) {
            return;
        }
        try {
            ttlRabbitTemplate.convertAndSendWithDelay(message.getExchangeName(), message.getRoutingKey(), message, delay.toMillis());
        } catch (Exception e) {
            log.error("MQ sendWithDelay failed: exchange={}, routingKey={}, delay={}", 
                    message.getExchangeName(), message.getRoutingKey(), delay, e);
        }
    }

    @Override
    public void sendAsyncWithDelay(MqCommonMessage<?> message, Duration delay) {
        if (message == null) {
            return;
        }
        try {
            ttlRabbitTemplate.convertAndSendWithDelay(message.getExchangeName(), message.getRoutingKey(), message, delay.toMillis());
        } catch (Exception e) {
            log.error("MQ sendAsyncWithDelay failed: exchange={}, routingKey={}, delay={}", 
                    message.getExchangeName(), message.getRoutingKey(), delay, e);
        }
    }
}