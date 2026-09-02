package com.psi.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 缓存配置
 * 统一缓存序列化、过期时间及 CacheManager
 */
@AutoConfiguration
@EnableCaching
public class RedisConfig {

    /**
     * 默认缓存过期时间：30 分钟
     */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer(objectMapper)))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .transactionAware()
                .build();
    }

    private GenericJackson2JsonRedisSerializer jsonSerializer(ObjectMapper objectMapper) {
        ObjectMapper cacheMapper = objectMapper.copy();
        cacheMapper.registerModule(new JavaTimeModule());
        cacheMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        cacheMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(cacheMapper);
    }
}
