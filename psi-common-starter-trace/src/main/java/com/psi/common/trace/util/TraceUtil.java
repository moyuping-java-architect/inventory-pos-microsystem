package com.psi.common.trace.util;

import com.psi.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 追踪工具类
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
public class TraceUtil {

    /**
     * 生成新的traceId
     */
    public static String generateTraceId() {
        try {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(uuid.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        }
    }

    /**
     * 生成新的spanId
     */
    public static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 获取或初始化traceId
     */
    public static String getOrInitTraceId() {
        String existingTraceId = UserContext.getTraceId();
        if (existingTraceId != null && !existingTraceId.isEmpty()) {
            return existingTraceId;
        }
        String newTraceId = generateTraceId();
        UserContext.setTraceId(newTraceId);
        return newTraceId;
    }

    /**
     * 清理前打印 traceID 和 JVM 栈信息（只有在错误时打印）
     */
    public static void logBeforeClear(boolean hasError) {
        if (!hasError) {
            return;
        }
        String traceId = getCurrentTraceId();
        log.info("══════════════════════════════════════════════════════════════════════");
        log.info("                      Trace Cleanup - Error Occurred                     ");
        log.info("══════════════════════════════════════════════════════════════════════");
        log.info("  traceId: {}", traceId);
        log.info("  JVM Stack Trace:");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            String indent = "    ";
            if (i > 0) {
                indent = "      ";
            }
            log.info("{}{}. {}", indent, i, element.toString());
        }
        log.info("══════════════════════════════════════════════════════════════════════");
    }

    /**
     * 获取当前traceId
     */
    public static String getCurrentTraceId() {
        return UserContext.getTraceId();
    }

    /**
     * 构建日志前缀
     *
     * @param traceId 追踪ID
     * @param serviceName 服务名称
     * @return 日志前缀
     */
    public static String buildLogPrefix(String traceId, String serviceName) {
        return String.format("[%s][%s]", serviceName, traceId != null ? traceId : "N/A");
    }

    /**
     * 构建日志前缀（带spanId）
     *
     * @param traceId 追踪ID
     * @param serviceName 服务名称
     * @param spanId 跨度ID
     * @return 日志前缀
     */
    public static String buildLogPrefix(String traceId, String serviceName, String spanId) {
        return String.format("[%s][%s][%s]", serviceName,
                traceId != null ? traceId : "N/A",
                spanId != null ? spanId : "N/A");
    }

}