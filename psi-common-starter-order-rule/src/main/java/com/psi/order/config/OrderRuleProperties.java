package com.psi.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单规则配置属性类
 * 支持通过配置文件自定义订单相关规则
 */
@Data
@Component
@ConfigurationProperties(prefix = "psi.order.rule")
public class OrderRuleProperties {

    /**
     * 是否启用订单规则
     */
    private boolean enabled = true;

    /**
     * 订单审批完成后是否发送MQ通知
     */
    private boolean sendMqOnCompleted = true;

    /**
     * 订单流程完成交换机名称（用于覆盖默认值）
     */
    private String completedExchange;

    /**
     * 订单流程完成路由键（用于覆盖默认值）
     */
    private String completedRoutingKey;

    /**
     * 订单最大审批天数
     */
    private int maxApproveDays = 7;

    /**
     * 是否启用自动取消超时订单
     */
    private boolean autoCancelTimeoutOrder = false;

    /**
     * 订单超时时间（小时）
     */
    private int orderTimeoutHours = 24;
}