package com.psi.message.aspect;

import com.psi.message.entity.MsgMessage;
import com.psi.message.mapper.MsgMessageMapper;
import com.psi.common.annotation.RabbitConsumerACK;
import com.psi.common.message.MqCommonMessage;
import com.rabbitmq.client.Channel;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * RabbitMQ消费者ACK切面
 * 
 * <p>拦截所有带有 {@link RabbitConsumerACK} 注解的方法，实现统一的ACK处理逻辑
 * 
 * <p>处理流程：
 * <ol>
 *   <li>拦截消费者方法</li>
 *   <li>提取 Channel、Message 和 MqCommonMessage 对象</li>
 *   <li>执行业务方法</li>
 *   <li>消费成功：更新消息表状态为已消费(2)</li>
 *   <li>消费失败：更新消息表状态为失败(3)并记录错误信息</li>
 *   <li>根据注解配置自动处理ACK/NACK</li>
 * </ol>
 * 
 * <p>消息状态说明：
 * <ul>
 *   <li>0 - 待发送</li>
 *   <li>1 - 已发送</li>
 *   <li>2 - 已消费</li>
 *   <li>3 - 消费失败</li>
 * </ul>
 * 
 * @author PSI
 * @version 1.1.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RabbitConsumerACKAspect {

    private final MsgMessageMapper msgMessageMapper;

    /**
     * 消息状态常量
     */
    private static final int STATUS_PENDING = 0;      // 待发送
    private static final int STATUS_SENT = 1;         // 已发送
    private static final int STATUS_CONSUMED = 2;     // 已消费
    private static final int STATUS_FAILED = 3;       // 消费失败

    /**
     * 定义切点：所有带有 @RabbitConsumerACK 注解的方法
     */
    @Pointcut("@annotation(com.psi.common.annotation.RabbitConsumerACK)")
    public void rabbitConsumerACKPointcut() {
    }

    /**
     * 环绕通知：统一处理ACK逻辑和消息状态更新
     * 
     * @param joinPoint        连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("rabbitConsumerACKPointcut()")
    public Object aroundConsumerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        
        // 提取 Channel、Message 和 MqCommonMessage 对象
        Channel channel = extractChannel(joinPoint.getArgs());
        Message message = extractMessage(joinPoint.getArgs());
        MqCommonMessage<?> mqCommonMessage = extractMqCommonMessage(joinPoint.getArgs());
        
        long deliveryTag = message != null ? message.getMessageProperties().getDeliveryTag() : -1;
        String messageId = mqCommonMessage != null ? mqCommonMessage.getMessageId() : null;
        
        log.debug("RabbitConsumerACK aspect intercepting: method={}, deliveryTag={}, messageId={}", 
                methodName, deliveryTag, messageId);

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 消费成功：更新消息表状态为已消费
            updateMessageStatus(messageId, STATUS_CONSUMED, null);
            
            // 成功时自动ACK
            if (channel != null) {
                try {
                    channel.basicAck(deliveryTag, false);
                    log.debug("Auto ACK success: method={}, deliveryTag={}, messageId={}", 
                            methodName, deliveryTag, messageId);
                } catch (Exception e) {
                    log.error("Failed to auto ACK: method={}, deliveryTag={}", methodName, deliveryTag, e);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            log.error("Consumer method exception: method={}, deliveryTag={}, messageId={}, error={}", 
                    methodName, deliveryTag, messageId, errorMsg, e);
            
            // 消费失败：更新消息表状态为失败并记录错误信息
            updateMessageStatus(messageId, STATUS_FAILED, errorMsg);
            
            // 异常时自动NACK
            if (channel != null) {
                try {
                  //  boolean requeue = rabbitConsumerACK.requeueOnError();
                    channel.basicNack(deliveryTag, false, false);
                    log.debug("Auto NACK: method={}, deliveryTag={}, messageId={}, requeue={}", 
                            methodName, deliveryTag, messageId, false);
                } catch (Exception ackEx) {
                    log.error("Failed to auto NACK: method={}, deliveryTag={}", methodName, deliveryTag, ackEx);
                }
            }
            
            throw e;
        }
    }

    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status    状态值
     * @param errorMsg  错误信息（失败时填写）
     */
    private void updateMessageStatus(String messageId, int status, String errorMsg) {
        if (messageId == null || messageId.isEmpty()) {
            log.debug("messageId is null, skip update status");
            return;
        }
        
        try {
            // 使用 UpdateWrapper 设置 WHERE 条件
            UpdateWrapper<MsgMessage> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("message_id", messageId)
                        .eq("del_flag", 0);
            
            // 设置要更新的字段
            MsgMessage msgMessage = new MsgMessage();
            msgMessage.setMsgStatus(status);
            msgMessage.setConsumeTime(System.currentTimeMillis());
            msgMessage.setUpdateTime(java.time.LocalDateTime.now());
            
            if (status == STATUS_FAILED && errorMsg != null) {
                // 错误信息截断，避免超过数据库字段长度
                if (errorMsg.length() > 500) {
                    errorMsg = errorMsg.substring(0, 500) + "...";
                }
                msgMessage.setErrorMsg(errorMsg);
            }
            
            // 执行更新（只更新 msg_status, consume_time, error_msg, update_time 字段）
            int result = msgMessageMapper.update(msgMessage, updateWrapper);
            log.info("Message status updated: messageId={}, status={}, affectedRows={}", messageId, status, result);
        } catch (Exception e) {
            log.error("Failed to update message status: messageId={}, status={}", messageId, status, e);
        }
    }

    /**
     * 从方法参数中提取 Channel 对象
     * 
     * @param args 方法参数
     * @return Channel 对象，未找到返回 null
     */
    private Channel extractChannel(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Channel) {
                return (Channel) arg;
            }
        }
        return null;
    }

    /**
     * 从方法参数中提取 Message 对象
     * 
     * @param args 方法参数
     * @return Message 对象，未找到返回 null
     */
    private Message extractMessage(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Message) {
                return (Message) arg;
            }
        }
        return null;
    }

    /**
     * 从方法参数中提取 MqCommonMessage 对象
     * 
     * @param args 方法参数
     * @return MqCommonMessage 对象，未找到返回 null
     */
    private MqCommonMessage<?> extractMqCommonMessage(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof MqCommonMessage) {
                return (MqCommonMessage<?>) arg;
            }
        }
        return null;
    }
}