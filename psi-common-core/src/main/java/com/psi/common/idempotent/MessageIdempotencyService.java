package com.psi.common.idempotent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 消息幂等校验服务
 *
 * <p>基于 Redis 实现，用于防止 MQ 消息重复消费。
 * 以 messageId 作为唯一键，24 小时过期。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageIdempotencyService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 幂等键前缀
     */
    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:message:";

    /**
     * 默认过期时间（24 小时）
     */
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    /**
     * 判断消息是否已处理
     *
     * @param messageId 消息唯一标识
     * @return true-已处理，false-未处理
     */
    public boolean isProcessed(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return false;
        }
        Boolean exists = stringRedisTemplate.hasKey(buildKey(messageId));
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 标记消息已处理
     *
     * @param messageId 消息唯一标识
     * @return true-标记成功，false-已存在（重复消息）
     */
    public boolean markProcessed(String messageId) {
        return markProcessed(messageId, DEFAULT_TTL);
    }

    /**
     * 标记消息已处理
     *
     * @param messageId 消息唯一标识
     * @param ttl       过期时间
     * @return true-标记成功，false-已存在（重复消息）
     */
    public boolean markProcessed(String messageId, Duration ttl) {
        if (messageId == null || messageId.isEmpty()) {
            log.warn("messageId is empty, skip mark processed");
            return false;
        }
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(buildKey(messageId), "1", ttl != null ? ttl : DEFAULT_TTL);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 清除幂等标记（业务失败时调用，允许下次重试）
     *
     * @param messageId 消息唯一标识
     */
    public void clearProcessed(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return;
        }
        stringRedisTemplate.delete(buildKey(messageId));
    }

    /**
     * 原子性地校验并执行业务逻辑
     *
     * <p>先原子占坑，如果已被占用说明是重复消息，直接返回 null；
     * 否则执行业务逻辑，成功后保持标记，失败后清除标记允许重试。</p>
     *
     * @param messageId 消息唯一标识
     * @param action    业务逻辑
     * @param <T>       返回值类型
     * @return 业务逻辑返回值；重复消息返回 null
     */
    public <T> T execute(String messageId, Supplier<T> action) {
        return execute(messageId, DEFAULT_TTL, action);
    }

    /**
     * 原子性地校验并执行业务逻辑
     *
     * @param messageId 消息唯一标识
     * @param ttl       过期时间
     * @param action    业务逻辑
     * @param <T>       返回值类型
     * @return 业务逻辑返回值；重复消息返回 null
     */
    public <T> T execute(String messageId, Duration ttl, Supplier<T> action) {
        if (messageId == null || messageId.isEmpty()) {
            log.warn("messageId is empty, execute directly");
            return action.get();
        }

        String key = buildKey(messageId);
        Boolean marked = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", ttl != null ? ttl : DEFAULT_TTL);

        if (!Boolean.TRUE.equals(marked)) {
            log.warn("Duplicate message detected, skip: messageId={}", messageId);
            return null;
        }

        try {
            return action.get();
        } catch (Exception e) {
            // 业务执行失败，删除幂等标记，允许下次重试
            stringRedisTemplate.delete(key);
            throw e;
        }
    }

    private String buildKey(String messageId) {
        return IDEMPOTENT_KEY_PREFIX + messageId;
    }
}
