package com.psi.order.strategy;

/**
 * 流程完成MQ发送策略接口
 * 定义不同业务类型的MQ路由信息
 */
public interface ProcessCompletedMqStrategy {

    String getBizType();

    String getExchange();

    String getRoutingKey();

    String getQueue();
}