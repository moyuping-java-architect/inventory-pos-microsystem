package com.psi.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

@Slf4j
@Component
public class RedisDistributedLock implements Lock {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String LOCK_PREFIX = "psi:lock:";
    private static final long DEFAULT_EXPIRE_MS = 30000;
    private static final long DEFAULT_WAIT_MS = 10000;
    private static final long DEFAULT_SLEEP_MS = 50;

    private final ThreadLocal<String> lockKey = new ThreadLocal<>();
    private final ThreadLocal<String> lockValue = new ThreadLocal<>();

    private static final RedisScript<Long> LOCK_SCRIPT;
    private static final RedisScript<Long> UNLOCK_SCRIPT;

    static {
        DefaultRedisScript<Long> lockScript = new DefaultRedisScript<>();
        lockScript.setScriptText(
                "if redis.call('setNx', KEYS[1], ARGV[1]) then " +
                "   redis.call('pexpire', KEYS[1], ARGV[2]) " +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end"
        );
        lockScript.setResultType(Long.class);
        LOCK_SCRIPT = lockScript;

        DefaultRedisScript<Long> unlockScript = new DefaultRedisScript<>();
        unlockScript.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "   redis.call('del', KEYS[1]) " +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end"
        );
        unlockScript.setResultType(Long.class);
        UNLOCK_SCRIPT = unlockScript;
    }

    public RedisDistributedLock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_MS);
    }

    public boolean tryLock(String key, long expireMs) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();

        Long result = stringRedisTemplate.execute(
                LOCK_SCRIPT,
                Collections.singletonList(lockKey),
                lockValue,
                String.valueOf(expireMs)
        );

        if (result != null && result == 1L) {
            this.lockKey.set(lockKey);
            this.lockValue.set(lockValue);
            return true;
        }
        return false;
    }

    public boolean lock(String key) {
        return lock(key, DEFAULT_EXPIRE_MS, DEFAULT_WAIT_MS);
    }

    public boolean lock(String key, long expireMs, long waitMs) {
        long start = System.currentTimeMillis();
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();

        do {
            Long result = stringRedisTemplate.execute(
                    LOCK_SCRIPT,
                    Collections.singletonList(lockKey),
                    lockValue,
                    String.valueOf(expireMs)
            );

            if (result != null && result == 1L) {
                this.lockKey.set(lockKey);
                this.lockValue.set(lockValue);
                return true;
            }

            try {
                Thread.sleep(DEFAULT_SLEEP_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } while (System.currentTimeMillis() - start < waitMs);

        return false;
    }

    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = this.lockValue.get();
        if (lockValue == null) {
            lockValue = UUID.randomUUID().toString();
        }

        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(lockKey),
                lockValue
        );
    }

    @Override
    public void lock() {
        throw new UnsupportedOperationException("Use lock(String key) instead");
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException("Use lock(String key) instead");
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException("Use tryLock(String key) instead");
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Use lock(String key, long expireMs, long waitMs) instead");
    }

    @Override
    public void unlock() {
        String key = lockKey.get();
        String value = lockValue.get();
        if (key != null && value != null) {
            stringRedisTemplate.execute(
                    UNLOCK_SCRIPT,
                    Collections.singletonList(key),
                    value
            );
            lockKey.remove();
            lockValue.remove();
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    public void lock(String key, Runnable runnable) {
        try {
            if (lock(key)) {
                runnable.run();
            }
        } finally {
            unlock(key);
        }
    }

    public <T> T lock(String key, Supplier<T> supplier) {
        try {
            if (lock(key)) {
                return supplier.get();
            }
            return null;
        } finally {
            unlock(key);
        }
    }
}
