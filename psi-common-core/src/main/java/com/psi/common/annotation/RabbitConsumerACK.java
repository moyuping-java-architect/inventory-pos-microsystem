package com.psi.common.annotation;

import java.lang.annotation.*;

/**
 * RabbitMQ消费者ACK注解
 * 
 * <p>用于标记需要手动ACK的RabbitMQ消费者方法，配合AOP切面实现统一的ACK处理逻辑
 * 
 * <p>使用示例：
 * <pre>
 * {@code
 * @RabbitListener(queues = "my.queue", ackMode = "MANUAL")
 * @RabbitConsumerACK
 * public void onMessage(Message message, Channel channel) {
 *     // 业务处理逻辑
 * }
 * }
 * </pre>
 * 
 * <p>注解属性：

 * 
 * @author PSI
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RabbitConsumerACK {
}