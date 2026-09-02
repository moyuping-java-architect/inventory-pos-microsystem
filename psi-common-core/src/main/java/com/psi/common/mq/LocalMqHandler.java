package com.psi.common.mq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本地 MQ 监听器注解（单机版，替代 RabbitMQ @RabbitListener）。
 * <p>
 * 标注在监听器方法上，声明该方法处理哪个 routingKey 的消息。
 * 一个 routingKey 可以有多个 @LocalMqHandler 方法，各自独立虚拟线程执行。
 * <p>
 * 用法：
 * <pre>
 * &#64;LocalMqHandler(routingKey = RabbitMQConstant.PROCESS_COMPLETED_SALE_ROUTING_KEY)
 * public void onProcessCompleted(MqCommonMessage&lt;?&gt; message) { ... }
 * </pre>
 *
 * @author PSI
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalMqHandler {

    /**
     * 路由键，对应 RabbitMQConstant 中定义的常量值
     */
    String routingKey();

    /**
     * 是否为三参数方法（MqCommonMessage, Message, Channel）。
     * 仅用于兼容原 @RabbitListener 的三参数签名（如 CashierSyncConsumer）。
     * 默认 false，即单参数 (MqCommonMessage)。
     */
    boolean threeParam() default false;
}
