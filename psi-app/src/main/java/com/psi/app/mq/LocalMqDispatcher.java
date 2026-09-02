package com.psi.app.mq;

import com.psi.common.mq.LocalMqHandler;
import com.psi.common.message.MqCommonMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 本地 MQ 分发器（单机版，替代 RabbitMQ Broker）。
 * <p>
 * 启动时扫描所有 Spring Bean 中标注了 {@link LocalMqHandler} 的方法，
 * 按 routingKey 建立注册表。一个 routingKey 可对应多个处理器，
 * 每个处理器在独立的虚拟线程中执行，互不阻塞。
 * <p>
 * 加消费者只需在方法上加 @LocalMqHandler 注解，无需修改本类。
 *
 * @author PSI
 */
@Slf4j
@Component
@Primary
public class LocalMqDispatcher implements SmartInitializingSingleton {

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final ApplicationContext applicationContext;
    private final Map<String, List<HandlerEntry>> handlerRegistry = new ConcurrentHashMap<>();

    public LocalMqDispatcher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        scanHandlers();
    }

    /**
     * 扫描所有 Bean 中标注了 @LocalMqHandler 的方法，建立路由注册表。
     */
    private void scanHandlers() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int handlerCount = 0;

        // 去重键：beanName + 方法名 + routingKey。
        // Spring 给 @Transactional 的监听器生成 CGLIB 代理后，
        // 代理类与原始类都持有同名方法，沿父类链向上遍历会把同一个
        // 处理器扫到两次，导致一条消息被消费两遍。
        Set<String> registered = new HashSet<>();

        for (String beanName : beanNames) {
            Class<?> beanType;
            try {
                beanType = applicationContext.getType(beanName);
            } catch (Exception e) {
                continue;
            }
            if (beanType == null) {
                continue;
            }

            // 遍历类及其父类的所有方法
            Class<?> current = beanType;
            while (current != null && current != Object.class) {
                for (Method method : current.getDeclaredMethods()) {
                    LocalMqHandler annotation = AnnotationUtils.findAnnotation(method, LocalMqHandler.class);
                    if (annotation == null) {
                        continue;
                    }

                    String routingKey = annotation.routingKey();
                    String dedupKey = beanName + "#" + method.getName() + "#" + routingKey;
                    if (!registered.add(dedupKey)) {
                        continue;
                    }

                    Object bean = applicationContext.getBean(beanName);
                    boolean threeParam = annotation.threeParam();

                    HandlerEntry entry = new HandlerEntry(bean, method, threeParam);
                    handlerRegistry
                            .computeIfAbsent(routingKey, k -> new CopyOnWriteArrayList<>())
                            .add(entry);
                    handlerCount++;

                    log.info("注册本地MQ监听器: routingKey={}, handler={}.{}()",
                            routingKey, beanType.getSimpleName(), method.getName());
                }
                current = current.getSuperclass();
            }
        }

        log.info("本地MQ监听器注册完成: {} 个路由键, {} 个处理器",
                handlerRegistry.size(), handlerCount);
    }

    /**
     * 分发消息到所有注册了该 routingKey 的处理器。
     * 每个处理器在独立的虚拟线程中执行，互不阻塞。
     */
    public void dispatch(MqCommonMessage<?> message) {
        if (message == null) {
            return;
        }

        String routingKey = message.getRoutingKey();
        List<HandlerEntry> handlers = handlerRegistry.get(routingKey);

        if (handlers == null || handlers.isEmpty()) {
            log.warn("本地MQ分发: 未匹配到路由键, routingKey={}", routingKey);
            return;
        }

        log.info("本地MQ分发: routingKey={}, messageId={}, handlers={}",
                routingKey, message.getMessageId(), handlers.size());

        for (HandlerEntry handler : handlers) {
            virtualThreadExecutor.submit(() -> {
                try {
                    invokeHandler(handler, message);
                } catch (Exception e) {
                    log.error("本地MQ分发异常: routingKey={}, messageId={}, handler={}.{}, error={}",
                            routingKey, message.getMessageId(),
                            handler.target().getClass().getSimpleName(), handler.method().getName(),
                            e.getMessage(), e);
                }
            });
        }
    }

    /**
     * 反射调用处理器方法。
     * threeParam=true 时兼容原 @RabbitListener 三参数签名 (message, amqpMessage, channel)。
     */
    @SuppressWarnings("unchecked")
    private void invokeHandler(HandlerEntry handler, MqCommonMessage<?> message) throws Exception {
        handler.method().setAccessible(true);
        if (handler.threeParam()) {
            handler.method().invoke(handler.target(), message, null, null);
        } else {
            handler.method().invoke(handler.target(), message);
        }
    }

    /**
     * 处理器条目：目标 Bean + 方法 + 是否三参数
     */
    private record HandlerEntry(Object target, Method method, boolean threeParam) {}
}
