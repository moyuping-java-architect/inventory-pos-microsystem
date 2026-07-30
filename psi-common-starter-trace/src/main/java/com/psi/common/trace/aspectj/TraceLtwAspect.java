package com.psi.common.trace.aspectj;

import com.psi.common.context.UserContext;
import com.psi.common.trace.config.TraceProperties;
import com.psi.common.trace.util.TraceContextCleaner;
import com.psi.common.trace.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AspectJ LTW 全方法追踪切面
 * 通过 Load-Time Weaving 技术拦截所有方法（private/protected/public/package-private）
 * 保留 traceId 上下文透传，支持超时检测和异常处理
 * 
 * 方案二：统一使用 AspectJ LTW 拦截所有方法
 * 
 * @author PSI
 * @version 2.2.0
 */
@Slf4j
@Aspect
public class TraceLtwAspect {

    private static volatile TraceProperties properties;

    /**
     * 高频方法集合（动态检测）
     */
    private static final ConcurrentHashMap<String, Long> methodCallCount = new ConcurrentHashMap<>();
    private static final Set<String> highFrequencyMethods = new java.util.concurrent.ConcurrentSkipListSet<>();

    /**
     * 高频方法阈值（调用超过此次数视为高频）
     */
    private static final long HIGH_FREQ_THRESHOLD = 100;

    /**
     * 自动跳过的方法前缀
     */
    private static final String[] SKIP_METHOD_PREFIXES = {"get", "set", "is", "hashCode", "equals", "toString", "clone", "finalize"};

    /**
     * 需要重点追踪的方法后缀（提高优先级）
     */
    private static final String[] TRACK_METHOD_SUFFIXES = {"Service", "Controller", "Client", "Impl", "Feign"};

    public static void setProperties(TraceProperties props) {
        properties = props;
    }

    /**
     * 拦截所有方法（AspectJ LTW 支持所有访问修饰符）
     * 切点表达式：排除配置类和基础设施类，避免启动时循环依赖
     * 含义：拦截 com.psi 包下所有类的所有方法，但排除 autoconfigure 和 interceptor 包
     */
    @Around("execution(* com.psi..*(..)) && " +
            "!execution(* com.psi..*AutoConfiguration*.*(..)) && " +
            "!execution(* com.psi..*Config*.*(..)) && " +
            "!execution(* com.psi..*Interceptor*.*(..)) && " +
            "!execution(* com.psi..*Properties*.*(..))")
    public Object traceAllMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 检查配置是否启用
        if (properties == null || !properties.isEnabled()) {
            return joinPoint.proceed();
        }

        // 2. 获取方法签名
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            return joinPoint.proceed();
        }

        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        String methodKey = buildMethodKey(method);

        // 3. 跳过系统方法和 getter/setter
        if (shouldSkipMethod(method)) {
            return joinPoint.proceed();
        }

        // 4. 跳过高频方法（非重点追踪方法）
        if (!isImportantMethod(method) && isHighFrequencyMethod(methodKey)) {
            return joinPoint.proceed();
        }

        // 5. 执行追踪逻辑
        return doTrace(joinPoint, method);
    }

    /**
     * 判断是否需要跳过该方法
     */
    private boolean shouldSkipMethod(Method method) {
        String methodName = method.getName();
        
        // 跳过 Object 类的方法
        if (method.getDeclaringClass() == Object.class) {
            return true;
        }
        
        // 跳过 getter/setter 等简单方法
        for (String prefix : SKIP_METHOD_PREFIXES) {
            if (methodName.startsWith(prefix)) {
                return true;
            }
        }
        
        // 跳过抽象方法和接口方法
        int modifiers = method.getModifiers();
        if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
            return true;
        }
        
        return false;
    }

    /**
     * 判断是否为重点追踪方法
     */
    private boolean isImportantMethod(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        for (String suffix : TRACK_METHOD_SUFFIXES) {
            if (className.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为高频方法
     */
    private boolean isHighFrequencyMethod(String methodKey) {
        if (highFrequencyMethods.contains(methodKey)) {
            return true;
        }

        long count = methodCallCount.merge(methodKey, 1L, Long::sum);
        if (count > HIGH_FREQ_THRESHOLD) {
            highFrequencyMethods.add(methodKey);
            log.info("[Trace] Marked as high frequency method: {}", methodKey);
            return true;
        }

        return false;
    }

    /**
     * 判断是否为入口方法
     */
    private boolean isEntryMethod(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        // Controller、FeignClient、MQ相关类、定时任务、异步方法视为入口方法
        return className.endsWith("Controller") || 
               className.endsWith("FeignClient") || 
               className.endsWith("Client") ||
               className.contains("Consumer") ||
               className.contains("Listener") ||
               className.contains("Scheduled") ||
               className.contains("Async");
    }

    /**
     * 执行实际追踪逻辑
     */
    private Object doTrace(ProceedingJoinPoint joinPoint, Method method) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取方法信息
        String methodName = method.getName();
        String className = joinPoint.getTarget() != null 
                ? joinPoint.getTarget().getClass().getSimpleName()
                : method.getDeclaringClass().getSimpleName();
        String spanName = className + "." + methodName;

        // 判断是否为入口方法（决定是否清理上下文）
        boolean isEntry = isEntryMethod(method);
        boolean isNestedCall = TraceContextCleaner.getDepth() > 0;
        
        // 如果是入口方法且不是嵌套调用，标记进入上下文
        if (isEntry && !isNestedCall) {
            TraceContextCleaner.enter();
        }

        // 获取或初始化 traceId
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

            // 检查是否超时
            long timeoutThreshold = properties.getMethodTimeoutThreshold();
            boolean isTimeout = duration > timeoutThreshold;

            // 仅在超时或异常时打印日志
            if (hasError) {
                logError(traceId, spanId, spanName, duration, exception);
            } else if (isTimeout) {
                logTimeout(traceId, spanId, spanName, duration, timeoutThreshold);
            }
            // 正常情况不打印日志

            // 只有入口方法才清理上下文（使用统一清理器）
            if (isEntry && !isNestedCall) {
                TraceContextCleaner.exit(hasError);
            }
        }
    }

    /**
     * 记录错误日志
     */
    private void logError(String traceId, String spanId, String spanName, 
                          long duration, Throwable exception) {
        log.error("══════════════════════════════════════════════════════════════════════");
        log.error("                         Method Error Occurred                          ");
        log.error("══════════════════════════════════════════════════════════════════════");
        log.error("  traceId: {}", traceId != null ? traceId : "N/A");
        log.error("  spanId: {}", spanId != null ? spanId : "N/A");
        log.error("  method: {}", spanName);
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
    private void logTimeout(String traceId, String spanId, String spanName,
                           long duration, long threshold) {
        log.warn("══════════════════════════════════════════════════════════════════════");
        log.warn("                          Method Timeout Warning                         ");
        log.warn("══════════════════════════════════════════════════════════════════════");
        log.warn("  traceId: {}", traceId != null ? traceId : "N/A");
        log.warn("  spanId: {}", spanId != null ? spanId : "N/A");
        log.warn("  method: {}", spanName);
        log.warn("  duration: {}ms (threshold: {}ms)", duration, threshold);
        log.warn("  Thread: {}", Thread.currentThread().getName());
        log.warn("══════════════════════════════════════════════════════════════════════");
    }

    /**
     * 构建方法唯一标识
     */
    private String buildMethodKey(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName() + "#" + method.getParameterCount();
    }
}