package com.psi.flow.mq.listener;

import com.psi.common.constant.RabbitMQConstant;
import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.order.dto.DocResponse;
import com.psi.order.service.DocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通用流程完成MQ监听器
 * 
 * <p>监听通用流程完成队列，处理通用单据审批通过后的状态更新
 * 通用单据不需要生成特定业务数据，仅标记单据为已完成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonProcessCompletedListener {

    private final DocService docService;
    private final MessageIdempotencyService messageIdempotencyService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMQConstant.PROCESS_COMPLETED_COMMON_QUEUE)
    public void onProcessCompleted(MqCommonMessage<?> message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String bizId = (String) data.get("bizId");
        String messageId = message.getMessageId();
        log.info("收到通用流程完成消息: bizId={}, messageId={}", bizId, messageId);

        messageIdempotencyService.execute(messageId, () -> {
            try {
                DocResponse doc = docService.findByDocNo(bizId);
                if (doc == null) {
                    log.warn("通用流程完成：单据不存在或查询失败: bizId={}", bizId);
                    return null;
                }

                // 单据状态已在 FlowEngineServiceImpl.updateDocStatus() 中更新，此处仅做日志记录
                log.info("通用流程完成消息已处理: bizId={}, docNo={}, status={}", bizId, doc.getDocNo(), doc.getStatus());
            } catch (Exception e) {
                log.error("处理通用流程完成消息失败: bizId={}, error={}", bizId, e.getMessage(), e);
                throw e;
            }
            return null;
        });
    }
}