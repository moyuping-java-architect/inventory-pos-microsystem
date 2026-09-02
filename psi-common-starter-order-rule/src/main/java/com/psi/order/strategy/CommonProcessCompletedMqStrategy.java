package com.psi.order.strategy;

import com.psi.order.constant.DocTypeConstant.BizType;
import com.psi.common.constant.RabbitMQConstant;
import org.springframework.stereotype.Component;

@Component
public class CommonProcessCompletedMqStrategy extends AbstractProcessCompletedMqStrategy {

    @Override
    public String getBizType() {
        return BizType.COMMON.getCode();
    }

    @Override
    public String getExchange() {
        return RabbitMQConstant.PROCESS_COMPLETED_COMMON_EXCHANGE;
    }

    @Override
    public String getRoutingKey() {
        return RabbitMQConstant.PROCESS_COMPLETED_COMMON_ROUTING_KEY;
    }

    @Override
    public String getQueue() {
        return RabbitMQConstant.PROCESS_COMPLETED_COMMON_QUEUE;
    }
}