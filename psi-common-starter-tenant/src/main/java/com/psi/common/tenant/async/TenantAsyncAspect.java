package com.psi.common.tenant.async;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * 异步方法租户上下文切面
 * 
 * <p>自动拦截所有 @Async 注解的方法，使用 TTL（TransmittableThreadLocal）确保租户上下文
 * 在异步线程中正确透传，执行完成后自动清理上下文。
 * 
 * <p>核心原理：
 * <ul>
 *   <li>获取当前线程的租户上下文</li>
 *   <li>使用 TtlRunnable 包装异步任务，确保上下文透传</li>
 *   <li>异步任务执行完成后自动清理上下文</li>
 * </ul>
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class TenantAsyncAspect {

    /**
     * 切入点：所有 @Async 注解的方法
     */
    @Pointcut("@annotation(org.springframework.scheduling.annotation.Async)")
    public void asyncMethods() {
    }

    /**
     * 环绕通知：使用TTL包装异步任务，确保租户上下文透传
     */
    @Around("asyncMethods()")
    public Object interceptAsyncMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前线程的租户上下文
        UserInfo currentUserInfo = UserContext.get();
        
        log.debug("Async method intercepted, tenant context: tenantId={}", 
                currentUserInfo != null ? currentUserInfo.getTenantId() : "null");

        // 获取方法签名判断是否有返回值
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getMethod().getReturnType();

        if (returnType == void.class || returnType == Void.class) {
            // 无返回值的异步方法
            Runnable ttlRunnable = TtlRunnable.get(() -> executeWithTenantContext(joinPoint, currentUserInfo));
            ttlRunnable.run();
            return null;
        } else {
            // 有返回值的异步方法
            Callable<Object> ttlCallable = TtlCallable.get(() -> executeWithTenantContextWithResult(joinPoint, currentUserInfo));
            return ttlCallable.call();
        }
    }

    /**
     * 在租户上下文环境中执行无返回值任务
     */
    private void executeWithTenantContext(ProceedingJoinPoint joinPoint, UserInfo currentUserInfo) {
        try {
            // 在异步线程中设置租户上下文
            if (currentUserInfo != null) {
                UserContext.set(currentUserInfo);
                log.debug("Tenant context set in async thread: tenantId={}", 
                        currentUserInfo.getTenantId());
            }
            
            // 执行原始方法
            joinPoint.proceed();
            
        } catch (Throwable e) {
            log.error("Async method execution failed", e);
            throw new RuntimeException(e);
        } finally {
            // 异步线程执行完成后清理上下文
            try {
                UserContext.clearAll();
                log.debug("Tenant context cleared in async thread");
            } catch (Exception e) {
                log.warn("Failed to clear tenant context in async thread", e);
            }
        }
    }

    /**
     * 在租户上下文环境中执行有返回值任务
     */
    private Object executeWithTenantContextWithResult(ProceedingJoinPoint joinPoint, UserInfo currentUserInfo) {
        try {
            // 在异步线程中设置租户上下文
            if (currentUserInfo != null) {
                UserContext.set(currentUserInfo);
                log.debug("Tenant context set in async thread: tenantId={}", 
                        currentUserInfo.getTenantId());
            }
            
            // 执行原始方法并返回结果
            return joinPoint.proceed();
            
        } catch (Throwable e) {
            log.error("Async method execution failed", e);
            throw new RuntimeException(e);
        } finally {
            // 异步线程执行完成后清理上下文
            try {
                UserContext.clearAll();
                log.debug("Tenant context cleared in async thread");
            } catch (Exception e) {
                log.warn("Failed to clear tenant context in async thread", e);
            }
        }
    }
}