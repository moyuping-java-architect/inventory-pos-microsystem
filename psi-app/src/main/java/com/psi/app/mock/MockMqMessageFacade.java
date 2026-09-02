package com.psi.app.mock;

import com.psi.app.mq.LocalMqDispatcher;
import com.psi.common.message.MqMessageFacade;
import com.psi.common.message.MqCommonMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * MQ 禁用时的 Mock 实现（虚拟线程本地分发替代 RabbitMQ）。
 * 对 LocalMqDispatcher 使用 @Lazy，避免与业务 Service 形成的 Bean 循环依赖。
 * 方法签名与原版 MqMessageFacade 保持一致，未来启用 RabbitMQ 时只需切换实现。
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "psi.mq.enabled", havingValue = "false")
public class MockMqMessageFacade implements MqMessageFacade {

    private final LocalMqDispatcher localMqDispatcher;

    @Autowired
    public MockMqMessageFacade(@Lazy LocalMqDispatcher localMqDispatcher) {
        this.localMqDispatcher = localMqDispatcher;
    }

    @Override
    public void send(MqCommonMessage<?> message) {
        if (message == null) {
            return;
        }
        log.info("Mock MQ send (sync): routingKey={}", message.getRoutingKey());
        try {
            localMqDispatcher.dispatch(message);
        } catch (Exception e) {
            log.error("Mock MQ send 失败: routingKey={}, error={}",
                    message.getRoutingKey(), e.getMessage(), e);
        }
    }

    @Override
    public void sendAsync(MqCommonMessage<?> message) {
        if (message == null) {
            return;
        }
        log.info("Mock MQ sendAsync: routingKey={}", message.getRoutingKey());
        try {
            // LocalMqDispatcher 内部已通过虚拟线程池异步提交任务
            localMqDispatcher.dispatch(message);
        } catch (Exception e) {
            log.error("Mock MQ sendAsync 失败: routingKey={}, error={}",
                    message.getRoutingKey(), e.getMessage(), e);
        }
    }

    @Override
    public void sendWithDelay(MqCommonMessage<?> message, Duration delay) {
        if (message == null) {
            return;
        }
        long delayMs = delay != null ? delay.toMillis() : 0;
        log.info("Mock MQ sendWithDelay: routingKey={}, delay={}ms", message.getRoutingKey(), delayMs);
        try {
            // 新开虚拟线程做延时后分发（不阻塞请求线程）
            Thread.startVirtualThread(() -> {
                try {
                    if (delayMs > 0) {
                        Thread.sleep(delayMs);
                    }
                    localMqDispatcher.dispatch(message);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("Mock MQ sendWithDelay 执行失败: routingKey={}, error={}",
                            message.getRoutingKey(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("Mock MQ sendWithDelay 提交失败: error={}", e.getMessage(), e);
        }
    }

    @Override
    public void sendAsyncWithDelay(MqCommonMessage<?> message, Duration delay) {
        sendWithDelay(message, delay);
    }
}
