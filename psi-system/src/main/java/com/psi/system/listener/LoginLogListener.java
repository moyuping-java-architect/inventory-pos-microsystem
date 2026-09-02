package com.psi.system.listener;

import com.psi.system.dto.SysLoginLogMessageDTO;
import com.psi.system.entity.SysLoginLog;
import com.psi.system.mapper.SysLoginLogMapper;
import com.psi.common.annotation.RabbitConsumerACK;
import com.psi.common.constant.RabbitMQConstant;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 登录日志监听器
 * 
 * <p>监听登录日志队列 {@link RabbitMQConstant#LOGIN_LOG_QUEUE}，
 * 使用手动ACK模式：先保存到数据库，成功后再确认ACK
 * 
 * <p>消费流程：
 * <ol>
 *   <li>接收MQ消息（从 {@link RabbitMQConstant#LOGIN_LOG_QUEUE} 队列）</li>
 *   <li>解析 {@link SysLoginLogMessageDTO}</li>
 *   <li>转换为 {@link SysLoginLog} 实体</li>
 *   <li>保存到 {@code sys_login_log} 表</li>
 *   <li><strong>手动ACK确认</strong></li>
 * </ol>
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogListener {

    private final SysLoginLogMapper sysLoginLogMapper;

    /**
     * 监听登录日志队列（手动ACK模式）
     * 
     * @param messageDTO 登录日志消息DTO
     * @param rabbitMsg  Spring AMQP Message 对象
     * @param channel    RabbitMQ Channel 对象
     * @throws Exception 处理异常时抛出，触发消息重投
     */
    @RabbitListener(queues = RabbitMQConstant.LOGIN_LOG_QUEUE, ackMode = "MANUAL")
    @RabbitConsumerACK
    public void onLoginLog(SysLoginLogMessageDTO messageDTO, Message rabbitMsg, Channel channel) throws Exception {
        // 获取消息投递标签（用于ACK确认）
        long deliveryTag = rabbitMsg.getMessageProperties().getDeliveryTag();
        
        log.info("Received login log message: username={}, success={}, deliveryTag={}",
                messageDTO.getUsername(),
                messageDTO.getSuccess(),
                deliveryTag);

        try {
            // 转换为实体对象
            SysLoginLog loginLog = convertToEntity(messageDTO);

            // 保存到数据库
            int result = sysLoginLogMapper.insert(loginLog);
            
            if (result > 0) {
                log.info("Login log saved successfully: username={}, id={}", 
                        messageDTO.getUsername(), loginLog.getId());
                
                // ✅ 手动ACK确认：消息处理成功
                channel.basicAck(deliveryTag, false);
                log.debug("Login log ACK confirmed: username={}, deliveryTag={}", 
                        messageDTO.getUsername(), deliveryTag);
            } else {
                log.warn("Login log save failed: username={}", messageDTO.getUsername());
                
                // ❌ 拒绝消息并重新投递（requeue=true）
                channel.basicNack(deliveryTag, false, true);
                log.warn("Login log NACK and requeue: username={}, deliveryTag={}", 
                        messageDTO.getUsername(), deliveryTag);
            }

        } catch (Exception e) {
            log.error("Failed to process login log: username={}", messageDTO.getUsername(), e);
            
            // ❌ 拒绝消息，不重新投递（进入死信队列）
            channel.basicNack(deliveryTag, false, false);
            log.error("Login log NACK (dead letter): username={}, deliveryTag={}", 
                    messageDTO.getUsername(), deliveryTag);
            
            throw new RuntimeException("Login log processing failed", e);
        }
    }

    /**
     * 将 SysLoginLogMessageDTO 转换为 SysLoginLog 实体
     * 
     * @param dto 登录日志消息DTO
     * @return SysLoginLog 实体
     */
    private SysLoginLog convertToEntity(SysLoginLogMessageDTO dto) {
        SysLoginLog entity = new SysLoginLog();
        
        // 处理 tenantId：如果是 "default" 或无效值，设为 0
        entity.setTenantId(convertTenantId(dto.getTenantId()));
        entity.setUserId(dto.getUserId());
        entity.setUsername(dto.getUsername());
        entity.setLoginType(dto.getLoginType());
        entity.setLoginTime(dto.getLoginTime());
        entity.setIpAddress(dto.getIpAddress());
        entity.setUserAgent(dto.getUserAgent());
        entity.setSuccess(dto.getSuccess());
        entity.setErrorMessage(dto.getErrorMessage());
        
        return entity;
    }

    /**
     * 转换租户ID字符串为Long类型
     * 
     * @param tenantIdStr 租户ID字符串
     * @return Long类型的租户ID，无效值返回0
     */
    private Long convertTenantId(String tenantIdStr) {
        if (tenantIdStr == null || tenantIdStr.isEmpty() || "default".equalsIgnoreCase(tenantIdStr)) {
            return 0L;
        }
        try {
            return Long.parseLong(tenantIdStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid tenantId: {}, defaulting to 0", tenantIdStr);
            return 0L;
        }
    }
}