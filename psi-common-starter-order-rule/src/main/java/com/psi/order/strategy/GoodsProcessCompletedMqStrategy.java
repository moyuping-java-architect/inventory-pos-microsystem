package com.psi.order.strategy;

import com.psi.order.constant.DocTypeConstant.BizType;
import com.psi.common.constant.RabbitMQConstant;
import org.springframework.stereotype.Component;

/**
 * 商品业务流程完成MQ策略
 * 处理商品信息审批等商品相关业务
 */
@Component
public class GoodsProcessCompletedMqStrategy extends AbstractProcessCompletedMqStrategy {

    @Override
    public String getBizType() {
        return BizType.GOODS.getCode();
    }

    @Override
    public String getExchange() {
        return RabbitMQConstant.PROCESS_COMPLETED_GOODS_EXCHANGE;
    }

    @Override
    public String getRoutingKey() {
        return RabbitMQConstant.PROCESS_COMPLETED_GOODS_ROUTING_KEY;
    }

    @Override
    public String getQueue() {
        return RabbitMQConstant.PROCESS_COMPLETED_GOODS_QUEUE;
    }
}