package com.psi.order.strategy;

/**
 * 流程完成MQ发送策略抽象基类
 * 各业务模块只需实现 getBizType/getExchange/getRoutingKey/getQueue
 */
public abstract class AbstractProcessCompletedMqStrategy implements ProcessCompletedMqStrategy {
}