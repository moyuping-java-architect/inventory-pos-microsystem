package com.psi.common.trace.mq;

import com.psi.common.context.UserContext;
import com.psi.common.trace.config.TraceProperties;
import com.psi.common.trace.util.TraceContextCleaner;
import com.psi.common.trace.util.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/**
 * MQ消费者追踪切面
 * 使用AspectJ LTW拦截@RabbitListener注解的方法
 * 确保消息消费时正确初始化和清理追踪上下文
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class TraceMqConsumerAspect {

    private final TraceProperties properties;

    /**
     * 拦截所有@RabbitListener注解的方法
     */
    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public Object interceptConsumer(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();

        // 提取消息对象
        Message message = extractMessage(joinPoint.getArgs());
        
        // 标记进入上下文
        TraceContextCleaner.enter();

        // 从消息头获取或初始化 traceId且设到 UserContext
        String traceId = extractTraceId(message);
        String spanId = TraceUtil.generateSpanId();

        String className = joinPoint.getTarget() != null 
                ? joinPoint.getTarget().getClass().getSimpleName()
                : joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Object result = null;
        Throwable exception = null;
        boolean hasError = false;

        try {
            result = joinPoint.proceed();
            return result;

        } catch (Throwable e) {
            exception = e;
            hasError = true;
            throw e;

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 检查是否超时
            long timeoutThreshold = properties.getMethodTimeoutThreshold();
            boolean isTimeout = duration > timeoutThreshold;

            // 仅在超时或异常时打印日志
            if (hasError) {
                logError(traceId, spanId, className, methodName, duration, exception);
            } else if (isTimeout) {
                logTimeout(traceId, spanId, className, methodName, duration, timeoutThreshold);
            }

            // 清理上下文
            TraceContextCleaner.exit(hasError);
        }
    }

    /**
     * 从方法参数中提取 Message 对象
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
     * 从消息头提取 traceId 并设置到 UserContext
     */
    private String extractTraceId(Message message) {
        String traceId;
        
        if (message != null) {
            MessageProperties messageProperties = message.getMessageProperties();
            if (messageProperties != null && messageProperties.getHeaders() != null) {
                Object traceIdObj = messageProperties.getHeaders().get(properties.getTraceIdHeader());
                if (traceIdObj != null) {
                    traceId = traceIdObj.toString();
                    UserContext.setTraceId(traceId);
                    log.debug("[Trace] MQ consumer received traceId from message: {}", traceId);
                    return traceId;
                }
            }
        }
        
        // 如果消息中没有 traceId，生成新的并设置到 UserContext
        traceId = TraceUtil.generateTraceId();
        UserContext.setTraceId(traceId);
        log.debug("[Trace] MQ consumer generated new traceId: {}", traceId);
        return traceId;
    }

    /**
     * 记录错误日志
     */
    private void logError(String traceId, String spanId, String className, String methodName,
                          long duration, Throwable exception) {
        log.error("══════════════════════════════════════════════════════════════════════");
        log.error("                        MQ Consumer Error Occurred                       ");
        log.error("══════════════════════════════════════════════════════════════════════");
        log.error("  traceId: {}", traceId != null ? traceId : "N/A");
        log.error("  spanId: {}", spanId != null ? spanId : "N/A");
        log.error("  consumer: {}.{}", className, methodName);
        log.error("  duration: {}ms", duration);
        log.error("  errorType: {}", exception.getClass().getName());
        log.error("  errorMessage: {}", exception.getMessage());
        log.error("  Thread: {}", Thread.currentThread().getName());
        log.error("  Stack Trace:");

        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (int i = 0; i < Math.min(stackTrace.length, 20); i++) {
            log.error("    at {}", stackTrace[i].toString());
        }

        log.error("══════════════════════════════════════════════════════════════════════");
    }

    /**
     * 记录超时日志
     */
    private void logTimeout(String traceId, String spanId, String className, String methodName,
                           long duration, long threshold) {
        log.warn("══════════════════════════════════════════════════════════════════════");
        log.warn("                         MQ Consumer Timeout Warning                      ");
        log.warn("══════════════════════════════════════════════════════════════════════");
        log.warn("  traceId: {}", traceId != null ? traceId : "N/A");
        log.warn("  spanId: {}", spanId != null ? spanId : "N/A");
        log.warn("  consumer: {}.{}", className, methodName);
        log.warn("  duration: {}ms (threshold: {}ms)", duration, threshold);
        log.warn("  Thread: {}", Thread.currentThread().getName());
        log.warn("══════════════════════════════════════════════════════════════════════");
    }
}