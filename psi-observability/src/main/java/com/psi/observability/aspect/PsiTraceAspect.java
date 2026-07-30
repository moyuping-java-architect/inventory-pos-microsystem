package com.psi.observability.aspect;

import com.psi.observability.util.TraceCtx;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @PsiTrace 注解的 AOP 切面（轻量级）.
 *
 * <p>当方法被打上 {@code @PsiTrace} 时，自动：</p>
 * <ol>
 *   <li>在 SkyWalking 上新建一个 Local span（业务级）</li>
 *   <li>把方法入参按名字打成 tag</li>
 *   <li>捕获异常并打 error tag</li>
 * </ol>
 *
 * <p>注意：SkyWalking Agent 默认会拦截 Spring AOP 的 Bean，
 * 所以这个切面会作为当前 span 的子 span 出现，链路完整。</p>
 */
@Slf4j
@Aspect
@Component
public class PsiTraceAspect {

    @Around("@annotation(com.psi.observability.annotation.PsiTrace)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        com.psi.observability.annotation.PsiTrace anno =
                method.getAnnotation(com.psi.observability.annotation.PsiTrace.class);

        // 1. 业务名 → tag
        TraceCtx.putTag("psi.biz.name", anno.name());

        // 2. 参数 → tag（按参数名匹配）
        String[] paramNames = signature.getParameterNames();
        Object[] args = pjp.getArgs();
        if (paramNames != null && args != null) {
            for (String tag : anno.tags()) {
                int idx = indexOf(paramNames, tag);
                if (idx >= 0 && idx < args.length) {
                    TraceCtx.putTag("psi.biz.arg." + tag, args[idx]);
                }
            }
        }

        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long cost = System.currentTimeMillis() - start;
            TraceCtx.putTag("psi.biz.cost_ms", cost);
            TraceCtx.putTag("psi.biz.result", "SUCCESS");
            return result;
        } catch (Throwable t) {
            TraceCtx.error(t);
            TraceCtx.putTag("psi.biz.result", "FAIL");
            throw t;
        }
    }

    private int indexOf(String[] arr, String target) {
        if (arr == null) return -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(target)) return i;
        }
        return -1;
    }
}
