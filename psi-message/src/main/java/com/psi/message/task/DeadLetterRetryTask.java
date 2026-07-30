package com.psi.message.task;

import com.psi.message.entity.MsgDeadLetter;
import com.psi.message.mapper.MsgDeadLetterMapper;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.util.JsonUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 死信自动重试图任务
 *
 * <p>定时扫描到达可重试时间的死信消息，按指数退避策略重新投递到原始队列。</p>
 *
 * @author PSI
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterRetryTask {

    private final MsgDeadLetterMapper msgDeadLetterMapper;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 基础重试间隔（分钟）
     */
    private static final int BASE_RETRY_INTERVAL_MINUTES = 5;

    /**
     * 最大重试间隔（分钟）
     */
    private static final int MAX_RETRY_INTERVAL_MINUTES = 60;

    /**
     * 定时重试任务（每1分钟执行一次）
     */
    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void retryDeadLetters() {
        log.info("Starting dead letter retry task...");

        LocalDateTime now = LocalDateTime.now();
        List<MsgDeadLetter> deadLetters = msgDeadLetterMapper.selectList(
                new LambdaQueryWrapper<MsgDeadLetter>()
                        .eq(MsgDeadLetter::getRetryable, 1)
                        .lt(MsgDeadLetter::getFailedCount, MAX_RETRY_COUNT)
                        .eq(MsgDeadLetter::getDelFlag, 0)
                        .le(MsgDeadLetter::getNextRetryTime, now)
        );

        log.info("Found {} dead letters ready to retry", deadLetters.size());

        for (MsgDeadLetter deadLetter : deadLetters) {
            try {
                // 先占坑：把 nextRetryTime 推到未来，避免本节点/其他实例并发重试同一条
                boolean locked = lockForRetry(deadLetter.getId(), now);
                if (!locked) {
                    log.debug("Dead letter is locked by another thread: id={}", deadLetter.getId());
                    continue;
                }

                boolean success = doRetry(deadLetter);
                updateStatus(deadLetter.getId(), success, deadLetter.getFailedCount(), now);

            } catch (Exception e) {
                log.error("Failed to retry dead letter: id={}, messageId={}",
                        deadLetter.getId(), deadLetter.getMessageId(), e);
            }
        }

        log.info("Dead letter retry task completed");
    }

    /**
     * 占坑：设置下次可重试时间为当前时间 + 一个较大间隔，防止并发
     */
    private boolean lockForRetry(Long id, LocalDateTime now) {
        LocalDateTime lockTime = now.plusMinutes(MAX_RETRY_INTERVAL_MINUTES);
        int updated = msgDeadLetterMapper.update(
                null,
                new LambdaUpdateWrapper<MsgDeadLetter>()
                        .set(MsgDeadLetter::getNextRetryTime, lockTime)
                        .eq(MsgDeadLetter::getId, id)
                        .le(MsgDeadLetter::getNextRetryTime, now)
        );
        return updated > 0;
    }

    /**
     * 计算下次可重试时间（指数退避）
     *
     * @param failedCount 当前失败次数
     * @param baseTime    基准时间
     * @return 下次可重试时间
     */
    private LocalDateTime calculateNextRetryTime(int failedCount, LocalDateTime baseTime) {
        int interval = Math.min(
                BASE_RETRY_INTERVAL_MINUTES * (1 << Math.max(0, failedCount - 1)),
                MAX_RETRY_INTERVAL_MINUTES
        );
        return baseTime.plusMinutes(interval);
    }

    /**
     * 执行重试
     */
    private boolean doRetry(MsgDeadLetter deadLetter) {
        try {
            String originalTopic = deadLetter.getOriginalTopic();
            if (originalTopic == null || !originalTopic.contains("/")) {
                log.warn("Invalid original topic: {}", originalTopic);
                return false;
            }

            String[] parts = originalTopic.split("/", 2);
            String exchange = parts[0];
            String routingKey = parts[1];

            String content = deadLetter.getContent();
            MqCommonMessage<?> message = JsonUtils.fromJson(content, MqCommonMessage.class);
            if (message == null) {
                log.warn("Failed to parse message content: {}", deadLetter.getId());
                return false;
            }

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Dead letter retried successfully: id={}, messageId={}",
                    deadLetter.getId(), deadLetter.getMessageId());
            return true;

        } catch (Exception e) {
            log.error("Error during retry: id={}", deadLetter.getId(), e);
            return false;
        }
    }

    /**
     * 更新状态
     *
     * @param id          死信ID
     * @param success     是否成功
     * @param failedCount 重试前的失败次数
     * @param now         当前时间
     */
    private void updateStatus(Long id, boolean success, int failedCount, LocalDateTime now) {
        LambdaUpdateWrapper<MsgDeadLetter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MsgDeadLetter::getId, id);

        if (success) {
            // 重试成功：失败次数清零，下次重试时间置空，标记为不可重试（已处理）
            updateWrapper
                    .set(MsgDeadLetter::getFailedCount, 0)
                    .set(MsgDeadLetter::getRetryable, 0)
                    .set(MsgDeadLetter::getNextRetryTime, (LocalDateTime) null)
                    .set(MsgDeadLetter::getLastFailedTime, now);
        } else {
            int newFailedCount = failedCount + 1;
            if (newFailedCount >= MAX_RETRY_COUNT) {
                // 超过最大重试次数：标记为不可重试
                updateWrapper
                        .setSql("failed_count = failed_count + 1")
                        .set(MsgDeadLetter::getRetryable, 0)
                        .set(MsgDeadLetter::getNextRetryTime, (LocalDateTime) null)
                        .set(MsgDeadLetter::getLastFailedTime, now);
            } else {
                // 未超过最大次数：指数退避
                LocalDateTime nextRetryTime = calculateNextRetryTime(newFailedCount, now);
                updateWrapper
                        .setSql("failed_count = failed_count + 1")
                        .set(MsgDeadLetter::getNextRetryTime, nextRetryTime)
                        .set(MsgDeadLetter::getLastFailedTime, now);
            }
        }

        msgDeadLetterMapper.update(null, updateWrapper);
        log.debug("Dead letter status updated: id={}, success={}", id, success);
    }
}
