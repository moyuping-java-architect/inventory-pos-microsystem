package com.psi.common.util;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID 工具类
 * 提供多种 ID 生成方式：雪花算法、UUID 等
 * 
 * @author PSI
 * @version 1.0.0
 */
public final class IdUtils {

    private IdUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // ==================== UUID 相关方法 ====================

    /**
     * 生成标准 UUID（带连字符）
     * 
     * @return UUID 字符串，格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成不带连字符的 UUID
     * 
     * @return UUID 字符串，格式：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     */
    public static String uuidWithoutHyphen() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成短 UUID（22位）
     * 使用 Base64 编码压缩 UUID，去掉末尾可能的 "=="
     * 
     * @return 短 UUID 字符串（22位）
     */
    public static String shortUuid() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >>> (8 * (7 - i)));
            bytes[i + 8] = (byte) (lsb >>> (8 * (7 - i)));
        }
        
        String base64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return base64;
    }

    /**
     * 生成 UUID（大写）
     * 
     * @return 大写 UUID 字符串
     */
    public static String uuidUpperCase() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    /**
     * 生成不带连字符的大写 UUID
     * 
     * @return 大写 UUID 字符串（不带连字符）
     */
    public static String uuidUpperCaseWithoutHyphen() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    // ==================== 雪花算法相关方法 ====================

    /**
     * 默认数据中心 ID（0-31）
     */
    private static final long DEFAULT_DATA_CENTER_ID = 1L;

    /**
     * 默认机器 ID（0-31）
     */
    private static final long DEFAULT_WORKER_ID = 1L;

    /**
     * 雪花算法 ID 生成器实例
     */
    private static final SnowflakeGenerator SNOWFLAKE_GENERATOR = new SnowflakeGenerator(DEFAULT_DATA_CENTER_ID, DEFAULT_WORKER_ID);

    /**
     * 生成雪花算法 ID（Long 类型）
     * 
     * @return 雪花算法 ID
     */
    public static long snowflakeId() {
        return SNOWFLAKE_GENERATOR.nextId();
    }

    /**
     * 生成雪花算法 ID（String 类型）
     * 
     * @return 雪花算法 ID 字符串
     */
    public static String snowflakeIdStr() {
        return String.valueOf(SNOWFLAKE_GENERATOR.nextId());
    }

    /**
     * 获取雪花算法生成器实例
     * 
     * @return SnowflakeGenerator 实例
     */
    public static SnowflakeGenerator getSnowflakeGenerator() {
        return SNOWFLAKE_GENERATOR;
    }

    /**
     * 创建自定义配置的雪花算法生成器
     * 
     * @param dataCenterId 数据中心 ID（0-31）
     * @param workerId 机器 ID（0-31）
     * @return SnowflakeGenerator 实例
     */
    public static SnowflakeGenerator createSnowflakeGenerator(long dataCenterId, long workerId) {
        return new SnowflakeGenerator(dataCenterId, workerId);
    }

    // ==================== 其他 ID 生成方法 ====================

    /**
     * 生成随机数字 ID
     * 
     * @param length 数字长度
     * @return 随机数字字符串
     */
    public static String randomNumericId(int length) {
        if (length <= 0) {
            return "";
        }
        
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        
        // 首位不能为 0
        sb.append(random.nextInt(9) + 1);
        
        for (int i = 1; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }

    /**
     * 生成带前缀的 ID
     * 
     * @param prefix 前缀
     * @return 带前缀的 ID，格式：prefix + 雪花算法 ID
     */
    public static String generateIdWithPrefix(String prefix) {
        return prefix + snowflakeIdStr();
    }

    /**
     * 生成带前缀和后缀的 ID
     * 
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 带前缀和后缀的 ID，格式：prefix + 雪花算法 ID + suffix
     */
    public static String generateIdWithPrefixSuffix(String prefix, String suffix) {
        return prefix + snowflakeIdStr() + suffix;
    }

    /**
     * 生成时间戳 + 随机数 ID
     * 
     * @return 时间戳 + 6位随机数
     */
    public static String timestampRandomId() {
        SecureRandom random = new SecureRandom();
        long timestamp = System.currentTimeMillis();
        int randomNum = random.nextInt(1000000);
        return String.format("%d%06d", timestamp, randomNum);
    }

    /**
     * 生成唯一 ID（默认使用雪花算法）
     * 
     * @return 雪花算法 ID 字符串
     */
    public static String generateId() {
        return snowflakeIdStr();
    }

    // ==================== 雪花算法内部类 ====================

    /**
     * 雪花算法生成器
     * 
     * 结构：
     * - 1 bit: 符号位（始终为 0）
     * - 41 bits: 时间戳（毫秒级，约69年）
     * - 5 bits: 数据中心 ID（0-31）
     * - 5 bits: 机器 ID（0-31）
     * - 12 bits: 序列号（0-4095）
     */
    public static class SnowflakeGenerator {

        /**
         * 开始时间戳（2024-01-01 00:00:00）
         */
        private static final long START_TIMESTAMP = 1704067200000L;

        /**
         * 数据中心 ID 占用位数
         */
        private static final long DATA_CENTER_ID_BITS = 5L;

        /**
         * 机器 ID 占用位数
         */
        private static final long WORKER_ID_BITS = 5L;

        /**
         * 序列号占用位数
         */
        private static final long SEQUENCE_BITS = 12L;

        /**
         * 数据中心 ID 最大值
         */
        private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

        /**
         * 机器 ID 最大值
         */
        private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

        /**
         * 序列号最大值
         */
        private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

        /**
         * 机器 ID 左移位数
         */
        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

        /**
         * 数据中心 ID 左移位数
         */
        private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

        /**
         * 时间戳左移位数
         */
        private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

        /**
         * 数据中心 ID
         */
        private final long dataCenterId;

        /**
         * 机器 ID
         */
        private final long workerId;

        /**
         * 序列号（原子操作）
         */
        private final AtomicLong sequence = new AtomicLong(0L);

        /**
         * 上次生成 ID 的时间戳
         */
        private volatile long lastTimestamp = -1L;

        /**
         * 构造函数
         * 
         * @param dataCenterId 数据中心 ID（0-31）
         * @param workerId 机器 ID（0-31）
         */
        public SnowflakeGenerator(long dataCenterId, long workerId) {
            if (dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID) {
                throw new IllegalArgumentException(
                    String.format("Data center ID must be between 0 and %d", MAX_DATA_CENTER_ID)
                );
            }
            if (workerId < 0 || workerId > MAX_WORKER_ID) {
                throw new IllegalArgumentException(
                    String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID)
                );
            }
            this.dataCenterId = dataCenterId;
            this.workerId = workerId;
        }

        /**
         * 生成下一个 ID
         * 
         * @return 雪花算法 ID
         */
        public long nextId() {
            long timestamp = getCurrentTimestamp();

            // 检查时钟回拨
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate ID for %d milliseconds", lastTimestamp - timestamp)
                );
            }

            // 如果是同一毫秒内，递增序列号
            if (timestamp == lastTimestamp) {
                long nextSequence = sequence.incrementAndGet();
                // 序列号溢出，等待下一毫秒
                if (nextSequence > MAX_SEQUENCE) {
                    timestamp = waitNextMillis(lastTimestamp);
                }
            } else {
                // 新的毫秒，重置序列号
                sequence.set(0L);
            }

            lastTimestamp = timestamp;

            // 组装 ID
            return (timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT
                    | dataCenterId << DATA_CENTER_ID_SHIFT
                    | workerId << WORKER_ID_SHIFT
                    | sequence.get();
        }

        /**
         * 生成下一个 ID（字符串形式）
         * 
         * @return 雪花算法 ID 字符串
         */
        public String nextIdStr() {
            return String.valueOf(nextId());
        }

        /**
         * 获取当前时间戳
         * 
         * @return 当前毫秒时间戳
         */
        private long getCurrentTimestamp() {
            return System.currentTimeMillis();
        }

        /**
         * 等待下一毫秒
         * 
         * @param lastTimestamp 上次生成 ID 的时间戳
         * @return 新的时间戳
         */
        private long waitNextMillis(long lastTimestamp) {
            long timestamp = getCurrentTimestamp();
            while (timestamp <= lastTimestamp) {
                timestamp = getCurrentTimestamp();
            }
            return timestamp;
        }

        /**
         * 获取数据中心 ID
         * 
         * @return 数据中心 ID
         */
        public long getDataCenterId() {
            return dataCenterId;
        }

        /**
         * 获取机器 ID
         * 
         * @return 机器 ID
         */
        public long getWorkerId() {
            return workerId;
        }

        /**
         * 获取上次生成 ID 的时间戳
         * 
         * @return 上次时间戳
         */
        public long getLastTimestamp() {
            return lastTimestamp;
        }
    }
}