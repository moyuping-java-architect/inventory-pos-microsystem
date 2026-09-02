package com.psi.common.async.util;

/**
 * 延迟时间工具类
 * 
 * @author PSI
 * @version 1.0.0
 */
public final class DelayUtils {

    private DelayUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 秒转换为毫秒
     */
    public static long secondsToMillis(int seconds) {
        return (long) seconds * 1000;
    }

    /**
     * 分钟转换为毫秒
     */
    public static long minutesToMillis(int minutes) {
        return (long) minutes * 60 * 1000;
    }

    /**
     * 小时转换为毫秒
     */
    public static long hoursToMillis(int hours) {
        return (long) hours * 60 * 60 * 1000;
    }

    /**
     * 天转换为毫秒
     */
    public static long daysToMillis(int days) {
        return (long) days * 24 * 60 * 60 * 1000;
    }
}