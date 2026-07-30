package com.psi.common.context;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 虚拟线程上下文包装器
 * 包装 VirtualThreadUtil 的所有方法，确保完整的用户上下文正确传递和清理
 * 
 * 核心特性：
 * 1. 保存并传递完整的 UserInfo 对象
 * 2. 保存并传递 traceId
 * 3. 任务完成后自动清理所有上下文，防止内存泄漏
 * 
 * 使用方式：
 * 1. 直接调用静态方法，如 VirtualThreadContextWrapper.executeAsync(() -> {...})
 * 2. 这些方法会自动处理上下文的传递和清理
 * 
 * @author PSI
 * @version 2.0.0
 */
@Slf4j
public final class VirtualThreadContextWrapper {

    private VirtualThreadContextWrapper() {
        // 私有构造函数
    }

    /**
     * 包装 Runnable 任务，确保完整上下文传递和清理
     */
    private static Runnable wrapRunnable(Runnable task) {
        // 保存完整的当前上下文
        UserInfo currentUserInfo = UserContext.get();
        String currentTraceId = UserContext.getTraceId();
        
        return () -> {
            // 设置完整上下文（继承父线程）
            if (currentUserInfo != null) {
                // 创建副本，避免并发修改问题
                UserInfo copy = new UserInfo();
                copy.setTenantId(currentUserInfo.getTenantId());
                copy.setShopId(currentUserInfo.getShopId());
                copy.setWarehouseId(currentUserInfo.getWarehouseId());
                copy.setUpdateUserId(currentUserInfo.getUpdateUserId());
                copy.setUpdateUserName(currentUserInfo.getUpdateUserName());
                copy.setRoleId(currentUserInfo.getRoleId());
                copy.setRoleName(currentUserInfo.getRoleName());
                copy.setPermissions(currentUserInfo.getPermissions());
                UserContext.set(copy);
            }
            
            if (currentTraceId != null) {
                UserContext.setTraceId(currentTraceId);
            }
            
            try {
                task.run();
            } finally {
                // 清理上下文
                UserContext.clearAll();
                log.debug("[VirtualThread] Context cleaned");
            }
        };
    }

    /**
     * 包装 Callable 任务，确保完整上下文传递和清理
     */
    private static <T> Callable<T> wrapCallable(Callable<T> task) {
        // 保存完整的当前上下文
        UserInfo currentUserInfo = UserContext.get();
        String currentTraceId = UserContext.getTraceId();
        
        return () -> {
            // 设置完整上下文（继承父线程）
            if (currentUserInfo != null) {
                // 创建副本，避免并发修改问题
                UserInfo copy = new UserInfo();
                copy.setTenantId(currentUserInfo.getTenantId());
                copy.setShopId(currentUserInfo.getShopId());
                copy.setWarehouseId(currentUserInfo.getWarehouseId());
                copy.setUpdateUserId(currentUserInfo.getUpdateUserId());
                copy.setUpdateUserName(currentUserInfo.getUpdateUserName());
                copy.setRoleId(currentUserInfo.getRoleId());
                copy.setRoleName(currentUserInfo.getRoleName());
                copy.setPermissions(currentUserInfo.getPermissions());
                UserContext.set(copy);
            }
            
            if (currentTraceId != null) {
                UserContext.setTraceId(currentTraceId);
            }
            
            try {
                return task.call();
            } finally {
                // 清理上下文
                UserContext.clearAll();
            }
        };
    }

    // ==================== 包装 VirtualThreadUtil 的方法 ====================

    /**
     * 执行单个异步任务（包装后）
     */
    public static Future<?> executeAsync(Runnable task) {
        return VirtualThreadUtil.executeAsync(wrapRunnable(task));
    }

    /**
     * 执行带返回值的异步任务（包装后）
     */
    public static <T> Future<T> executeAsync(Callable<T> task) {
        return VirtualThreadUtil.executeAsync(wrapCallable(task));
    }

    /**
     * 批量执行任务（包装后）
     */
    public static <T> List<T> executeBatch(List<T> items, Function<T, T> processor) {
        return VirtualThreadUtil.executeBatch(items, item -> {
            try {
                return processor.apply(item);
            } finally {
                UserContext.clearAll();
            }
        });
    }

    /**
     * 分批异步处理（包装后）
     */
    public static <T> void executeBatchStreaming(List<T> items, int batchSize, Consumer<List<T>> processor) {
        VirtualThreadUtil.executeBatchStreaming(items, batchSize, batch -> {
            try {
                processor.accept(batch);
            } finally {
                UserContext.clearAll();
            }
        });
    }

    /**
     * 并行执行多个任务，返回第一个完成的结果（包装后）
     */
    public static <T> T executeAny(List<Callable<T>> tasks) {
        List<Callable<T>> wrappedTasks = tasks.stream()
                .map(VirtualThreadContextWrapper::wrapCallable)
                .collect(Collectors.toList());
        return VirtualThreadUtil.executeAny(wrappedTasks);
    }

    /**
     * 带超时的异步执行（包装后）
     */
    public static <T> T executeWithTimeout(Callable<T> task, long timeout, java.util.concurrent.TimeUnit unit) {
        return VirtualThreadUtil.executeWithTimeout(wrapCallable(task), timeout, unit);
    }

    /**
     * 延迟执行任务（包装后）
     */
    public static void executeDelayed(Runnable task, long delay, java.util.concurrent.TimeUnit unit) {
        VirtualThreadUtil.executeDelayed(wrapRunnable(task), delay, unit);
    }
}