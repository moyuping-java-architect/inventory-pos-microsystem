package com.psi.common.trace.scheduled;

import com.psi.common.context.UserContext;
import com.psi.common.trace.config.TraceProperties;
import com.psi.common.trace.util.TraceContextCleaner;
import com.psi.common.trace.util.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 定时任务追踪切面
 * 支持 @Scheduled 注解的定时任务，确保追踪上下文正确初始化和清理
 * 
 * 核心特性：
 * 1. 在定时任务执行前初始化 traceId（定时任务没有外部调用者）
 * 2. 在虚拟线程中正确清理 ThreadLocal
 * 3. 记录任务执行时间、超时和异常信息
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class TraceScheduledAspect {

    private final TraceProperties properties;

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object interceptScheduledMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String spanName = "Scheduled." + className + "." + methodName;

        return executeScheduledTask(spanName, joinPoint);
    }

    private Object executeScheduledTask(String spanName, ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        TraceContextCleaner.enter();
        String traceId = TraceUtil.getOrInitTraceId();
        String spanId = TraceUtil.generateSpanId();

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
            long timeoutThreshold = properties.getMethodTimeoutThreshold();
            boolean isTimeout = duration > timeoutThreshold;

            // 仅在异常或超时场景打印日志
            if (hasError) {
                logError(traceId, spanId, spanName, duration, exception);
            } else if (isTimeout) {
                logTimeout(traceId, spanId, spanName, duration, timeoutThreshold);
            }

            cleanUpContext(hasError);
        }
    }

    private void cleanUpContext(boolean hasError) {
        try {
            if (Thread.currentThread().isVirtual()) {
                log.debug("[Trace] Cleaning up context in virtual thread");
            }
            
            TraceContextCleaner.exit(hasError);
            
        } catch (Exception e) {
            log.error("[Trace] Error cleaning up context: {}", e.getMessage());
            UserContext.clearAll();
        }
    }

    private void logError(String traceId, String spanId, String spanName, 
                          long duration, Throwable exception) {
        log.error("══════════════════════════════════════════════════════════════════════");
        log.error("                      Scheduled Task Error Occurred                      ");
        log.error("══════════════════════════════════════════════════════════════════════");
        log.error("  traceId: {}", traceId != null ? traceId : "N/A");
        log.error("  spanId: {}", spanId != null ? spanId : "N/A");
        log.error("  method: {}", spanName);
        log.error("  duration: {}ms", duration);
        log.error("  errorType: {}", exception.getClass().getName());
        log.error("  errorMessage: {}", exception.getMessage());
        log.error("  Thread: {} (Virtual: {})", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        log.error("  Stack Trace:");

        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (int i = 0; i < Math.min(stackTrace.length, 20); i++) {
            log.error("    at {}", stackTrace[i].toString());
        }

        log.error("══════════════════════════════════════════════════════════════════════");
    }

    private void logTimeout(String traceId, String spanId, String spanName,
                           long duration, long threshold) {
        log.warn("══════════════════════════════════════════════════════════════════════");
        log.warn("                       Scheduled Task Timeout Warning                     ");
        log.warn("══════════════════════════════════════════════════════════════════════");
        log.warn("  traceId: {}", traceId != null ? traceId : "N/A");
        log.warn("  spanId: {}", spanId != null ? spanId : "N/A");
        log.warn("  method: {}", spanName);
        log.warn("  duration: {}ms (threshold: {}ms)", duration, threshold);
        log.warn("  Thread: {} (Virtual: {})", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        log.warn("══════════════════════════════════════════════════════════════════════");
    }
}