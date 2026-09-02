package com.psi.order.strategy;

import com.psi.order.constant.DocTypeConstant.BizType;
import com.psi.common.constant.RabbitMQConstant;
import org.springframework.stereotype.Component;

/**
 * 销售业务流程完成MQ策略
 * 处理销售订单、销售出库单等销售相关业务
 */
@Component
public class SaleProcessCompletedMqStrategy extends AbstractProcessCompletedMqStrategy {

    @Override
    public String getBizType() {
        return BizType.SALE.getCode();
    }

    @Override
    public String getExchange() {
        return RabbitMQConstant.PROCESS_COMPLETED_SALE_EXCHANGE;
    }

    @Override
    public String getRoutingKey() {
        return RabbitMQConstant.PROCESS_COMPLETED_SALE_ROUTING_KEY;
    }

    @Override
    public String getQueue() {
        return RabbitMQConstant.PROCESS_COMPLETED_SALE_QUEUE;
    }
}