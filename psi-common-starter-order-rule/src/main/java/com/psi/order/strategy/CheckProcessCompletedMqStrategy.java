package com.psi.order.strategy;

import com.psi.order.constant.DocTypeConstant.BizType;
import com.psi.common.constant.RabbitMQConstant;
import org.springframework.stereotype.Component;

/**
 * 盘点业务流程完成MQ策略
 * 处理盘点单业务，调整库存数量
 */
@Component
public class CheckProcessCompletedMqStrategy extends AbstractProcessCompletedMqStrategy {

    @Override
    public String getBizType() {
        return BizType.CHECK.getCode();
    }

    @Override
    public String getExchange() {
        return RabbitMQConstant.PROCESS_COMPLETED_CHECK_EXCHANGE;
    }

    @Override
    public String getRoutingKey() {
        return RabbitMQConstant.PROCESS_COMPLETED_CHECK_ROUTING_KEY;
    }

    @Override
    public String getQueue() {
        return RabbitMQConstant.PROCESS_COMPLETED_CHECK_QUEUE;
    }
}