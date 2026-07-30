package com.psi.common.async.aspect;

import com.psi.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步方法租户上下文清理切面
 * 在异步方法执行完毕后清理 UserContext，避免线程复用时上下文污染
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class AsyncTenantAspect {

    /**
     * 定义切点：所有标注了 @Async 的方法
     */
    @Pointcut("@annotation(org.springframework.scheduling.annotation.Async)")
    public void asyncMethods() {}

    /**
     * 环绕通知：在异步方法执行后清理租户上下文
     * 
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("asyncMethods()")
    public Object aroundAsyncMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 执行异步方法
            return joinPoint.proceed();
        } finally {
            // 清理租户上下文（无论成功还是失败都执行）
            // 注意：这里只清理当前线程的上下文，不会影响父线程
            try {
                UserContext.clearAll();
                log.debug("Tenant context cleared after async method execution: {}", 
                        joinPoint.getSignature().getName());
            } catch (Exception e) {
                log.warn("Failed to clear tenant context after async method", e);
            }
        }
    }
}