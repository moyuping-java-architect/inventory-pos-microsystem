package com.psi.common.tenant.feign;

import com.psi.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Feign调用租户上下文清理切面
 * 
 * <p>自动拦截所有Feign客户端接口的调用，确保在Feign调用完成后（无论成功失败）
 * 清理发起方的租户上下文，防止上下文泄漏。
 * 
 * <p>适用场景：
 * <ul>
 *   <li>非Web环境调用Feign（如定时任务、异步方法）</li>
 *   <li>下游服务调用失败时的上下文清理</li>
 *   <li>需要在Feign调用后立即清理上下文的场景</li>
 * </ul>
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class TenantFeignAspect {

    /**
     * 切入点：所有 Feign 客户端接口的方法调用
     * 通过匹配带有 @FeignClient 注解的类的所有方法
     */
    @Pointcut("within(@org.springframework.cloud.openfeign.FeignClient *)")
    public void feignClientMethods() {
    }

    /**
     * 环绕通知：执行Feign调用 -> 清理上下文
     * 
     * <p>注意：此切面仅负责清理发起方的上下文，下游服务的上下文由下游自己管理。
     */
    @Around("feignClientMethods()")
    public Object interceptFeignCall(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 执行原始Feign调用
            return joinPoint.proceed();
            
        } finally {
            // 无论成功还是失败，都清理发起方的上下文
            // 注意：仅当当前线程是发起方（非Web请求线程）时才清理
            // Web请求的上下文由 TenantContextWebInterceptor 负责清理
            clearContextIfNonWebThread();
        }
    }

    /**
     * 在非Web线程中清理上下文
     * 
     * <p>通过检查是否存在请求相关的ThreadLocal来判断是否为Web线程。
     * 如果是Web线程，由Web拦截器负责清理；否则在此处清理。
     */
    private void clearContextIfNonWebThread() {
        try {
            // 判断是否为Web请求线程
            boolean isWebThread = isWebRequestThread();
            
            if (!isWebThread) {
                UserContext.clearAll();
                log.debug("Tenant context cleared after Feign call (non-web thread)");
            }
            // 如果是Web线程，由TenantContextWebInterceptor在afterCompletion中清理
        } catch (Exception e) {
            log.warn("Failed to clear tenant context after Feign call", e);
        }
    }

    /**
     * 判断当前线程是否为Web请求线程
     * 
     * <p>通过检查是否能获取到请求相关的对象来判断。
     * 如果是Web线程，会有RequestContextHolder等上下文。
     */
    private boolean isWebRequestThread() {
        try {
            // 尝试获取请求上下文，如果能获取到说明是Web线程
            org.springframework.web.context.request.RequestAttributes attrs = 
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            return attrs != null;
        } catch (Exception e) {
            // 如果类不存在或其他异常，认为不是Web线程
            return false;
        }
    }
}