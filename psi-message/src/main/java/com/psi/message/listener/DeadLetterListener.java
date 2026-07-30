package com.psi.message.listener;

import com.psi.message.dto.MsgDeadLetterSaveDTO;
import com.psi.message.dto.MsgDeadLetterTodoSaveDTO;
import com.psi.message.entity.MsgDeadLetter;
import com.psi.message.entity.MsgDeadLetterTodo;
import com.psi.message.service.MsgDeadLetterService;
import com.psi.message.service.MsgDeadLetterTodoService;
import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.JsonUtils;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 死信队列监听器
 * 
 * <p>监听公共死信队列 {@link RabbitMQConstant#COMMON_DLX_QUEUE}，处理所有死信消息：
 * <ol>
 *   <li>接收死信消息</li>
 *   <li>解析死信消息内容</li>
 *   <li>保存到死信表（msg_dead_letter）</li>
 *   <li>生成死信待办（msg_dead_letter_todo）</li>
 *   <li>手动ACK确认</li>
 * </ol>
 * 
 * <p>死信消息来源：
 * <ul>
 *   <li>消息被拒绝且不重新投递</li>
 *   <li>消息过期（TTL）</li>
 *   <li>队列达到最大长度</li>
 * </ul>
 * 
 * @author PSI
 * @version 1.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterListener {

    private final MsgDeadLetterService deadLetterService;
    private final MsgDeadLetterTodoService deadLetterTodoService;

    /**
     * 监听公共死信队列及按业务拆分的死信队列
     *
     * @param message   消息内容（自动反序列化为 MqCommonMessage，如果失败则为 null）
     * @param rabbitMsg Spring AMQP Message 对象，包含死信头信息
     * @param channel   RabbitMQ Channel 对象
     */
    @RabbitListener(queues = {
            RabbitMQConstant.COMMON_DLX_QUEUE,
            RabbitMQConstant.DLX_PURCHASE_QUEUE,
            RabbitMQConstant.DLX_SALE_QUEUE,
            RabbitMQConstant.DLX_STOCK_QUEUE,
            RabbitMQConstant.DLX_GOODS_QUEUE,
            RabbitMQConstant.DLX_COMMON_QUEUE,
            RabbitMQConstant.DLX_MESSAGE_RECORD_QUEUE,
            RabbitMQConstant.DLX_LOGIN_LOG_QUEUE,
            RabbitMQConstant.DLX_FINANCE_QUEUE,
            RabbitMQConstant.DLX_SYNC_QUEUE
    }, ackMode = "MANUAL")
    public void onDeadLetter(MqCommonMessage<?> message, Message rabbitMsg, Channel channel) {
        long deliveryTag = rabbitMsg.getMessageProperties().getDeliveryTag();
        DeadLetterInfo deadLetterInfo;

        // 1. 先解析消息；解析失败属于不可逆错误，直接 ACK 丢弃，避免无限循环
        try {
            deadLetterInfo = parseDeadLetter(message, rabbitMsg);
            log.info("Received dead letter: messageId={}, originalExchange={}, originalRoutingKey={}, reason={}",
                    deadLetterInfo.getMessageId(),
                    deadLetterInfo.getOriginalExchange(),
                    deadLetterInfo.getOriginalRoutingKey(),
                    deadLetterInfo.getReason());
        } catch (Exception e) {
            log.error("Failed to parse dead letter, ACK and discard: deliveryTag={}", deliveryTag, e);
            safeAck(channel, deliveryTag);
            return;
        }

        String messageId = deadLetterInfo.getMessageId();

        // 2. 保存到死信表并生成待办；落库失败时 NACK 重新入队，避免丢失死信
        try {
            Long deadLetterId = saveDeadLetter(deadLetterInfo);
            if (deadLetterId != null) {
                createDeadLetterTodo(deadLetterId, messageId);
            }
            channel.basicAck(deliveryTag, false);
            log.info("Dead letter processed successfully: messageId={}, deliveryTag={}", messageId, deliveryTag);
        } catch (Exception e) {
            log.error("Failed to persist dead letter, NACK and requeue: messageId={}, deliveryTag={}", messageId, deliveryTag, e);
            safeNack(channel, deliveryTag);
        }
    }

    private void safeAck(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception ackEx) {
            log.error("Failed to ACK dead letter: deliveryTag={}", deliveryTag, ackEx);
        }
    }

    private void safeNack(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (Exception ackEx) {
            log.error("Failed to NACK dead letter: deliveryTag={}", deliveryTag, ackEx);
        }
    }

    /**
     * 解析死信消息
     * 
     * @param message   已反序列化的消息对象（可能为 null）
     * @param rabbitMsg Spring AMQP Message 对象，包含死信头信息
     * @return DeadLetterInfo 死信信息
     */
    private DeadLetterInfo parseDeadLetter(MqCommonMessage<?> message, Message rabbitMsg) {
        DeadLetterInfo info = new DeadLetterInfo();
        MessageProperties properties = rabbitMsg.getMessageProperties();
        Map<String, Object> headers = properties.getHeaders();

        // 1. 优先从 x-death 头中提取原始交换机和路由键
        Object xDeath = headers != null ? headers.get("x-death") : null;
        String originalExchange = extractOriginalExchange(xDeath);
        String originalRoutingKey = extractOriginalRoutingKey(xDeath);

        // 2. 其次使用消息体中保存的原始路由信息
        if ((originalExchange == null || originalExchange.isEmpty()) && message != null) {
            originalExchange = getSafeString(message.getExchangeName());
        }
        if ((originalRoutingKey == null || originalRoutingKey.isEmpty()) && message != null) {
            originalRoutingKey = getSafeString(message.getRoutingKey());
        }

        // 3. 最后使用接收到的死信交换机和路由键
        if (originalExchange == null || originalExchange.isEmpty()) {
            originalExchange = getSafeString(properties.getReceivedExchange());
        }
        if (originalRoutingKey == null || originalRoutingKey.isEmpty()) {
            originalRoutingKey = getSafeString(properties.getReceivedRoutingKey());
        }

        info.setOriginalExchange(originalExchange);
        info.setOriginalRoutingKey(originalRoutingKey);

        // 获取死信原因（从 headers 中获取）
        if (headers != null) {
            // 死信原因
            if (xDeath != null) {
                try {
                    String deathJson = JsonUtils.toJson(xDeath);
                    info.setReason(extractDeathReason(deathJson));
                } catch (Exception e) {
                    log.warn("Failed to parse x-death header: {}", e.getMessage());
                    info.setReason("解析失败");
                }
            }

            // 原始队列
            Object originalQueue = headers.get("x-original-queue");
            if (originalQueue != null) {
                info.setOriginalQueue(originalQueue.toString());
            }
        }

        // 如果 headers 为空，默认死信原因
        if (info.getReason() == null) {
            info.setReason("未知原因");
        }

        // 优先使用已反序列化的 message 对象
        if (message != null) {
            info.setMessageId(getSafeString(message.getMessageId()));
            info.setSourceService(getSafeString(message.getSourceService()));
            info.setOperatorId(getSafeString(message.getOperatorId()));
            // 将消息序列化为 JSON 作为内容
            try {
                info.setContent(JsonUtils.toJson(message));
            } catch (Exception e) {
                log.warn("Failed to serialize message to JSON", e);
            }
        } else {
            // 如果没有反序列化成功，从 rabbitMsg 中解析
            byte[] body = rabbitMsg.getBody();
            String content = body != null ? new String(body, StandardCharsets.UTF_8) : "";
            info.setContent(content);

            // 尝试解析为 MqCommonMessage 获取 messageId
            try {
                if (!content.isEmpty()) {
                    MqCommonMessage<?> mqMessage = JsonUtils.fromJson(content, MqCommonMessage.class);
                    if (mqMessage != null) {
                        info.setMessageId(getSafeString(mqMessage.getMessageId()));
                        info.setSourceService(getSafeString(mqMessage.getSourceService()));
                        info.setOperatorId(getSafeString(mqMessage.getOperatorId()));
                    }
                }
            } catch (Exception e) {
                log.debug("Message is not MqCommonMessage format, using UUID as messageId");
            }
        }

        // 如果 messageId 仍为空，生成一个唯一ID
        if (info.getMessageId() == null || info.getMessageId().isEmpty()) {
            info.setMessageId("DLX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        }

        return info;
    }

    /**
     * 从 x-death 头中提取原始交换机
     *
     * @param xDeath x-death 头对象
     * @return 原始交换机名称
     */
    @SuppressWarnings("unchecked")
    private String extractOriginalExchange(Object xDeath) {
        if (!(xDeath instanceof java.util.List) || ((java.util.List<?>) xDeath).isEmpty()) {
            return null;
        }
        try {
            java.util.List<java.util.Map<String, Object>> deathList = (java.util.List<java.util.Map<String, Object>>) xDeath;
            // 取最后一条（最近一次死信）
            java.util.Map<String, Object> death = deathList.get(deathList.size() - 1);
            Object exchange = death.get("exchange");
            return exchange != null ? exchange.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract original exchange from x-death: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 x-death 头中提取原始路由键
     *
     * @param xDeath x-death 头对象
     * @return 原始路由键
     */
    @SuppressWarnings("unchecked")
    private String extractOriginalRoutingKey(Object xDeath) {
        if (!(xDeath instanceof java.util.List) || ((java.util.List<?>) xDeath).isEmpty()) {
            return null;
        }
        try {
            java.util.List<java.util.Map<String, Object>> deathList = (java.util.List<java.util.Map<String, Object>>) xDeath;
            java.util.Map<String, Object> death = deathList.get(deathList.size() - 1);
            Object routingKeys = death.get("routing-keys");
            if (routingKeys instanceof java.util.List && !((java.util.List<?>) routingKeys).isEmpty()) {
                return ((java.util.List<?>) routingKeys).get(0).toString();
            }
            Object routingKey = death.get("routing-key");
            return routingKey != null ? routingKey.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract original routing key from x-death: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 x-death 头中提取死信原因
     * 
     * @param deathJson x-death 头的 JSON 字符串
     * @return 死信原因描述
     */
    private String extractDeathReason(String deathJson) {
        if (deathJson == null || deathJson.isEmpty()) {
            return "未知原因";
        }
        
        try {
            // x-death 格式: [{"queue":"xxx","reason":"expired","time":...}]
            String cleanJson = deathJson;
            if (cleanJson.startsWith("[")) {
                cleanJson = cleanJson.substring(1);
            }
            if (cleanJson.endsWith("]")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 1);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> deathMap = JsonUtils.fromJson(cleanJson, Map.class);
            if (deathMap != null) {
                Object reason = deathMap.get("reason");
                if (reason != null) {
                    String reasonStr = reason.toString();
                    switch (reasonStr) {
                        case "expired":
                            return "消息过期(TTL)";
                        case "rejected":
                            return "消息被拒绝";
                        case "maxlen":
                            return "队列达到最大长度";
                        default:
                            return "未知原因: " + reasonStr;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract death reason: {}", e.getMessage());
        }
        return "未知原因";
    }

    /**
     * 保存死信到死信表
     * 
     * @param info 死信信息
     * @return 死信ID，如果保存失败返回 null
     */
    private Long saveDeadLetter(DeadLetterInfo info) {
        try {
            MsgDeadLetterSaveDTO saveDTO = new MsgDeadLetterSaveDTO();
            saveDTO.setMessageId(info.getMessageId());
            saveDTO.setOriginalTopic(info.getOriginalExchange() + "/" + info.getOriginalRoutingKey());
            saveDTO.setContent(truncateContent(info.getContent(), 4000));  // 限制内容长度
            saveDTO.setSender(info.getSourceService());
            saveDTO.setReceiver("psi-message");
            saveDTO.setReason(info.getReason());
            saveDTO.setFailedCount(1);
            // 根据死信原因自动判断是否可重试
            saveDTO.setRetryable(isRetryable(info.getReason()));
            // 首次进入死信，5 分钟后可重试
            saveDTO.setNextRetryTime(LocalDateTime.now().plusMinutes(5));

            CommonResult<com.psi.message.dto.MsgDeadLetterDTO> result = deadLetterService.save(saveDTO);
            
            if (result != null && result.getCode() == ResultCode.SUCCESS.getCode() && result.getData() != null) {
                Long deadLetterId = result.getData().getId();
                log.info("Dead letter saved: id={}, messageId={}", deadLetterId, info.getMessageId());
                return deadLetterId;
            } else {
                String errorMsg = result != null ? result.getMessage() : "Unknown error";
                log.error("Failed to save dead letter: messageId={}, error={}", info.getMessageId(), errorMsg);
                return null;
            }
        } catch (Exception e) {
            log.error("Error saving dead letter: messageId={}", info.getMessageId(), e);
            return null;
        }
    }

    /**
     * 生成死信待办
     * 
     * @param deadLetterId 死信ID
     * @param messageId    消息ID
     */
    private void createDeadLetterTodo(Long deadLetterId, String messageId) {
        try {
            MsgDeadLetterTodoSaveDTO saveDTO = new MsgDeadLetterTodoSaveDTO();
            saveDTO.setDeadLetterId(deadLetterId);
            saveDTO.setMessageId(messageId);
            saveDTO.setHandler("admin");  // 默认管理员处理
            saveDTO.setHandleType(1);     // 1-待审核
            
            CommonResult<com.psi.message.dto.MsgDeadLetterTodoDTO> result = deadLetterTodoService.save(saveDTO);
            
            if (result != null && result.getCode() == ResultCode.SUCCESS.getCode() && result.getData() != null) {
                log.info("Dead letter todo created: deadLetterId={}, todoId={}", 
                        deadLetterId, result.getData().getId());
            } else {
                String errorMsg = result != null ? result.getMessage() : "Unknown error";
                log.error("Failed to create dead letter todo: deadLetterId={}, error={}", deadLetterId, errorMsg);
            }
        } catch (Exception e) {
            log.error("Error creating dead letter todo: deadLetterId={}", deadLetterId, e);
        }
    }

    /**
     * 获取安全的字符串，避免 null
     * 
     * @param str 原始字符串
     * @return 安全的字符串，如果为 null 返回空字符串
     */
    private String getSafeString(String str) {
        return str != null ? str : "";
    }

    /**
     * 截断内容，避免超过数据库字段长度
     * 
     * @param content 原始内容
     * @param maxLength 最大长度
     * @return 截断后的内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength - 3) + "...";
    }

    /**
     * 根据死信原因判断是否可重试
     * 
     * @param reason 死信原因
     * @return 1-可重试，0-不可重试
     */
    private Integer isRetryable(String reason) {
        if (reason == null || reason.isEmpty()) {
            return 1;  // 默认可重试
        }
        
        // 可重试的原因
        if (reason.contains("过期") ||          // 消息过期(TTL)
            reason.contains("TTL") ||           // TTL过期
            reason.contains("maxlen") ||        // 队列达到最大长度
            reason.contains("队列达到最大")) {   // 队列达到最大长度
            return 1;
        }
        
        // 不可重试的原因（通常是业务逻辑问题）
        if (reason.contains("拒绝") ||          // 消息被拒绝
            reason.contains("Duplicate") ||     // 重复键错误
            reason.contains("Constraint") ||    // 约束违反
            reason.contains("不存在") ||        // 数据不存在
            reason.contains("权限") ||          // 权限问题
            reason.contains("参数错误")) {      // 参数错误
            return 0;
        }
        
        // 默认可重试（未知原因）
        return 1;
    }

    /**
     * 死信信息内部类
     */
    private static class DeadLetterInfo {
        private String messageId;
        private String originalExchange;
        private String originalRoutingKey;
        private String originalQueue;
        private String content;
        private String reason;
        private String sourceService;
        private String operatorId;

        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getOriginalExchange() { return originalExchange; }
        public void setOriginalExchange(String originalExchange) { this.originalExchange = originalExchange; }
        public String getOriginalRoutingKey() { return originalRoutingKey; }
        public void setOriginalRoutingKey(String originalRoutingKey) { this.originalRoutingKey = originalRoutingKey; }
        public String getOriginalQueue() { return originalQueue; }
        public void setOriginalQueue(String originalQueue) { this.originalQueue = originalQueue; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getSourceService() { return sourceService; }
        public void setSourceService(String sourceService) { this.sourceService = sourceService; }
        public String getOperatorId() { return operatorId; }
        public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    }
}