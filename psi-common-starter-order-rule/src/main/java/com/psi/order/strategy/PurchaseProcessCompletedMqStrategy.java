package com.psi.order.strategy;

import com.psi.order.constant.DocTypeConstant.BizType;
import com.psi.common.constant.RabbitMQConstant;
import org.springframework.stereotype.Component;

/**
 * 采购业务流程完成MQ策略
 * 处理采购订单、采购入库单等采购相关业务
 */
@Component
public class PurchaseProcessCompletedMqStrategy extends AbstractProcessCompletedMqStrategy {

    @Override
    public String getBizType() {
        return BizType.PURCHASE.getCode();
    }

    @Override
    public String getExchange() {
        return RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_EXCHANGE;
    }

    @Override
    public String getRoutingKey() {
        return RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_ROUTING_KEY;
    }

    @Override
    public String getQueue() {
        return RabbitMQConstant.PROCESS_COMPLETED_PURCHASE_QUEUE;
    }
}