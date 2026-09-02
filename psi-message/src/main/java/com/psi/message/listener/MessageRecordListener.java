package com.psi.message.listener;

import com.psi.message.entity.MsgMessage;
import com.psi.message.mapper.MsgMessageMapper;
import com.psi.common.annotation.RabbitConsumerACK;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.util.JsonUtils;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消息记录监听器
 * 
 * <p>监听消息持久化队列 {@link RabbitMQConstant#MESSAGE_RECORD_QUEUE}，
 * 使用手动ACK模式：先保存到数据库，成功后再确认ACK
 * 
 * <p>消费流程：
 * <ol>
 *   <li>接收MQ消息（从 {@link RabbitMQConstant#MESSAGE_RECORD_QUEUE} 队列）</li>
 *   <li>解析 {@link MqCommonMessage}</li>
 *   <li>转换为 {@link MsgMessage} 实体</li>
 *   <li>保存到 {@code mq_message_record} 表</li>
 *   <li><strong>手动ACK确认</strong></li>
 * </ol>
 * 
 * <p>手动ACK模式确保：只有数据库保存成功才会确认消息，
 * 保存失败时消息会重新投递或进入死信队列
 * 
 * @author PSI
 * @version 1.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRecordListener {

    private final MsgMessageMapper msgMessageMapper;

    /**
     * 监听消息持久化队列（手动ACK模式）
     * 
     * @param message     MqCommonMessage 消息对象
     * @param rabbitMsg   Spring AMQP Message 对象
     * @param channel     RabbitMQ Channel 对象
     * @throws Exception 处理异常时抛出，触发消息重投
     */
    @RabbitListener(queues = RabbitMQConstant.MESSAGE_RECORD_QUEUE, ackMode = "MANUAL")
    @RabbitConsumerACK
    public void onMessageRecord(MqCommonMessage<?> message, Message rabbitMsg, Channel channel) throws Exception {
        // 获取消息投递标签（用于ACK确认）
        long deliveryTag = rabbitMsg.getMessageProperties().getDeliveryTag();
        
        log.info("Received message record: messageId={}, exchange={}, routingKey={}, deliveryTag={}",
                message.getMessageId(),
                message.getExchangeName(),
                message.getRoutingKey(),
                deliveryTag);

        try {
            // 转换为实体对象
            MsgMessage msgMessage = convertToEntity(message);

            // 保存到数据库
            int result = msgMessageMapper.insert(msgMessage);
            
            if (result > 0) {
                log.info("Message record saved successfully: messageId={}, id={}", 
                        message.getMessageId(), msgMessage.getId());
            } else {
                log.warn("Message record save failed: messageId={}", message.getMessageId());
                // ACK/NACK 由 RabbitConsumerACKAspect 统一处理
            }

        } catch (Exception e) {
            log.error("Failed to process message record: messageId={}", message.getMessageId(), e);
            // ACK/NACK 由 RabbitConsumerACKAspect 统一处理
            throw new RuntimeException("Message record processing failed", e);
        }
    }

    /**
     * 将 MqCommonMessage 转换为 MsgMessage 实体
     * 
     * @param message MqCommonMessage 消息对象
     * @return MsgMessage 实体
     */
    private MsgMessage convertToEntity(MqCommonMessage<?> message) {
        MsgMessage entity = new MsgMessage();
        
        entity.setMessageId(message.getMessageId());
        entity.setTenantId(convertTenantId(message.getTenantId()));
        entity.setOperatorId(message.getOperatorId());
        // 确保 sourceService 不为空
        entity.setSourceService(message.getSourceService() != null ? message.getSourceService() : "unknown");
        entity.setExchangeName(message.getExchangeName());
        entity.setRoutingKey(message.getRoutingKey());
        entity.setEventType(message.getMessageType());
        
        if (message.getData() != null) {
            entity.setMessageBody(JsonUtils.toJson(message.getData()));
        }
        
        if (message.getExtParams() != null && !message.getExtParams().isEmpty()) {
            entity.setExtParams(JsonUtils.toJson(message.getExtParams()));
        }
        
        entity.setMsgStatus(1);  // 已发送状态
        entity.setSendTime(message.getCreateTime());
        
        return entity;
    }

    /**
     * 转换租户ID，处理非数字值
     * 
     * @param tenantId 租户ID字符串
     * @return 转换后的租户ID（Long类型）
     */
    private Long convertTenantId(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(tenantId);
        } catch (NumberFormatException e) {
            log.warn("Invalid tenantId format: {}, defaulting to 0", tenantId);
            return 0L;
        }
    }
}
