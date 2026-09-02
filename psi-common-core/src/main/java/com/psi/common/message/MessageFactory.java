package com.psi.common.message;

import com.psi.common.config.AppGlobalConfig;
import com.psi.common.context.UserContext;
import com.psi.common.util.IdUtils;

/**
 * 消息工厂类
 * 用于创建各种类型的MQ消息
 * 
 * 优化说明：
 * - 使用雪花算法生成 messageId，保证全局唯一且有序
 * - 相比 UUID，雪花ID更适合分布式场景，支持按时间排序
 */
public final class MessageFactory {

    private MessageFactory() {
        // 私有构造函数
    }

    /**
     * 创建基础消息
     * 
     * @param data 消息数据
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param messageType 消息类型
     * @return MqCommonMessage 实例
     */
    public static <T> MqCommonMessage<T> create(
            T data,
            String exchange,
            String routingKey,
            String messageType
            ) {
        MqCommonMessage<T> message = new MqCommonMessage<>();
        // 使用雪花算法生成唯一消息ID（替代UUID，支持分布式唯一且有序）
        message.setMessageId(IdUtils.snowflakeIdStr());
        message.setSourceService(AppGlobalConfig.getCurrentServiceName());
        message.setExchangeName(exchange);
        message.setRoutingKey(routingKey);
        message.setMessageType(messageType);
        message.setData(data);
        message.setTenantId(UserContext.getTenantId());
        message.setOperatorId(UserContext.getUserId());
        message.setCreateTime(System.currentTimeMillis());
        return message;
    }

}