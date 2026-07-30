package com.psi.common.trace.util;

import com.psi.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 追踪上下文清理器
 * 确保在所有场景下正确清理 UserContext，防止内存泄漏
 * 
 * 设计原则：
 * 1. 只在入口方法处清理上下文
 * 2. 使用 try-finally 确保一定清理
 * 3. 支持嵌套调用检测，避免重复清理
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
public final class TraceContextCleaner {

    /**
     * 线程本地计数器，用于追踪嵌套调用深度
     */
    private static final ThreadLocal<Integer> CLEAN_DEPTH = ThreadLocal.withInitial(() -> 0);

    /**
     * 线程本地标记，标记是否已经打印过清理日志
     */
    private static final ThreadLocal<Boolean> LOGGED = ThreadLocal.withInitial(() -> false);

    private TraceContextCleaner() {
        // 私有构造函数，防止实例化
    }

    /**
     * 进入追踪上下文（在入口方法开始时调用）
     */
    public static void enter() {
        int depth = CLEAN_DEPTH.get();
        CLEAN_DEPTH.set(depth + 1);
        LOGGED.set(false);
        log.debug("[Trace] Entering trace context, depth={}", depth + 1);
    }

    /**
     * 退出追踪上下文（在入口方法结束时调用）
     * 
     * @param hasError 是否发生错误
     */
    public static void exit(boolean hasError) {
        int depth = CLEAN_DEPTH.get();
        
        if (depth > 0) {
            depth--;
            CLEAN_DEPTH.set(depth);
            
            log.debug("[Trace] Exiting trace context, depth={}", depth);
            
            // 只有在最外层退出时才清理上下文
            if (depth == 0) {
                try {
                    // 只有在发生错误且未打印过时才打印清理日志
                    if (hasError && !LOGGED.get()) {
                        TraceUtil.logBeforeClear(true);
                        LOGGED.set(true);
                    }
                } finally {
                    // 确保上下文一定被清理
                    UserContext.clearAll();
                    CLEAN_DEPTH.remove();
                    LOGGED.remove();
                    log.debug("[Trace] Context fully cleared");
                }
            }
        }
    }

    /**
     * 强制清理上下文（用于异常情况）
     */
    public static void forceClean() {
        try {
            String traceId = UserContext.getTraceId();
            log.warn("[Trace] Forcing context cleanup, traceId={}", traceId);
            TraceUtil.logBeforeClear(true);
        } finally {
            UserContext.clearAll();
            CLEAN_DEPTH.remove();
            LOGGED.remove();
        }
    }

    /**
     * 获取当前嵌套深度
     */
    public static int getDepth() {
        return CLEAN_DEPTH.get();
    }

    /**
     * 检查是否需要清理（是否在最外层）
     */
    public static boolean shouldClean() {
        return CLEAN_DEPTH.get() == 1;
    }
}