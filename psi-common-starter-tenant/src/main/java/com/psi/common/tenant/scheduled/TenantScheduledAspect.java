package com.psi.common.tenant.scheduled;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * 定时任务租户上下文切面
 * 
 * <p>自动拦截所有 @Scheduled 注解的定时任务方法，使用 TTL（TransmittableThreadLocal）
 * 确保租户上下文在定时任务执行时正确设置和清理。
 * 
 * <p>注意：定时任务执行时通常没有请求上下文，因此需要提前设置租户信息。
 * 可以通过以下方式设置：
 * <ul>
 *   <li>在配置类中预先设置默认租户</li>
 *   <li>使用 @TenantId 注解指定租户</li>
 *   <li>在任务内部手动设置</li>
 * </ul>
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class TenantScheduledAspect {

    /**
     * 切入点：所有 @Scheduled 注解的方法
     */
    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void scheduledMethods() {
    }

    /**
     * 环绕通知：设置租户上下文 -> 执行任务 -> 清理上下文
     */
    @Around("scheduledMethods()")
    public Object interceptScheduledMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前线程的租户上下文（定时任务可能没有前置上下文）
        UserInfo currentUserInfo = UserContext.get();
        
        log.debug("Scheduled method intercepted, tenant context: tenantId={}", 
                currentUserInfo != null ? currentUserInfo.getTenantId() : "null");

        // 获取方法签名判断是否有返回值
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getMethod().getReturnType();

        if (returnType == void.class || returnType == Void.class) {
            // 无返回值的定时任务
            Runnable ttlRunnable = TtlRunnable.get(() -> executeWithTenantContext(joinPoint, currentUserInfo));
            ttlRunnable.run();
            return null;
        } else {
            // 有返回值的定时任务
            Callable<Object> ttlCallable = TtlCallable.get(() -> executeWithTenantContextWithResult(joinPoint, currentUserInfo));
            return ttlCallable.call();
        }
    }

    /**
     * 在租户上下文环境中执行无返回值定时任务
     */
    private void executeWithTenantContext(ProceedingJoinPoint joinPoint, UserInfo currentUserInfo) {
        try {
            // 在定时任务线程中设置租户上下文
            if (currentUserInfo != null) {
                UserContext.set(currentUserInfo);
                log.debug("Tenant context set in scheduled thread: tenantId={}", 
                        currentUserInfo.getTenantId());
            }
            
            // 执行原始定时任务方法
            joinPoint.proceed();
            
        } catch (Throwable e) {
            log.error("Scheduled method execution failed", e);
            throw new RuntimeException(e);
        } finally {
            // 定时任务执行完成后清理上下文
            try {
                UserContext.clearAll();
                log.debug("Tenant context cleared in scheduled thread");
            } catch (Exception e) {
                log.warn("Failed to clear tenant context in scheduled thread", e);
            }
        }
    }

    /**
     * 在租户上下文环境中执行有返回值定时任务
     */
    private Object executeWithTenantContextWithResult(ProceedingJoinPoint joinPoint, UserInfo currentUserInfo) {
        try {
            // 在定时任务线程中设置租户上下文
            if (currentUserInfo != null) {
                UserContext.set(currentUserInfo);
                log.debug("Tenant context set in scheduled thread: tenantId={}", 
                        currentUserInfo.getTenantId());
            }
            
            // 执行原始定时任务方法并返回结果
            return joinPoint.proceed();
            
        } catch (Throwable e) {
            log.error("Scheduled method execution failed", e);
            throw new RuntimeException(e);
        } finally {
            // 定时任务执行完成后清理上下文
            try {
                UserContext.clearAll();
                log.debug("Tenant context cleared in scheduled thread");
            } catch (Exception e) {
                log.warn("Failed to clear tenant context in scheduled thread", e);
            }
        }
    }
}