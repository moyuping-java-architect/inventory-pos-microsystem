package com.psi.common.context;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JDK 21 虚拟线程工具类（核心版）
 * 提供基础的虚拟线程异步执行能力，支持 TTL 上下文透传
 * 
 * 核心特性：
 * 1. 支持 TTL（TransmittableThreadLocal）上下文自动透传
 * 2. 使用虚拟线程实现高并发异步执行
 * 3. 极低的内存开销，适合 I/O 密集型任务
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
public final class VirtualThreadUtil {

    /**
     * 虚拟线程执行器（单例）
     */
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private VirtualThreadUtil() {
        // 私有构造函数
    }

    /**
     * 执行单个异步任务（自动传递 TTL 上下文）
     */
    public static Future<?> executeAsync(Runnable task) {
        return VIRTUAL_EXECUTOR.submit(TtlRunnable.get(task));
    }

    /**
     * 执行带返回值的异步任务（自动传递 TTL 上下文）
     */
    public static <T> Future<T> executeAsync(Callable<T> task) {
        return VIRTUAL_EXECUTOR.submit(TtlCallable.get(task));
    }

    /**
     * 批量执行任务（并行处理）
     */
    public static <T> List<T> executeBatch(List<T> items, Function<T, T> processor) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompletableFuture<T>> futures = new ArrayList<>();
        
        for (T item : items) {
            final T finalItem = item;
            CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return processor.apply(finalItem);
                } catch (Exception e) {
                    log.error("批量任务处理失败，item: {}, error: {}", finalItem, e.getMessage());
                    return null;
                }
            }, VIRTUAL_EXECUTOR);
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 收集结果
        return futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(result -> result != null)
                .toList();
    }

    /**
     * 分批异步处理（流式处理，防OOM）
     */
    public static <T> void executeBatchStreaming(List<T> items, int batchSize, Consumer<List<T>> processor) {
        if (items == null || items.isEmpty()) {
            return;
        }

        int totalSize = items.size();
        int batches = (int) Math.ceil((double) totalSize / batchSize);
        
        log.info("开始分批处理，总数: {}, 批次: {}, 每批: {}", totalSize, batches, batchSize);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < batches; i++) {
            final int start = i * batchSize;
            final int end = Math.min(start + batchSize, totalSize);
            List<T> batch = items.subList(start, end);

            int finalI = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    processor.accept(batch);
                    log.debug("批次 {} 处理完成，数量: {}", finalI + 1, batch.size());
                } catch (Exception e) {
                    log.error("批次 {} 处理失败: {}", finalI + 1, e.getMessage());
                }
            }, VIRTUAL_EXECUTOR);

            futures.add(future);
        }

        // 等待所有批次完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("所有批次处理完成");
    }

    /**
     * 并行执行多个任务，返回第一个完成的结果
     */
    public static <T> T executeAny(List<Callable<T>> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }

        List<Callable<T>> wrappedTasks = tasks.stream()
                .map(TtlCallable::get)
                .collect(Collectors.toList());

        try {
            return VIRTUAL_EXECUTOR.invokeAny(wrappedTasks, 30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("并行任务执行失败: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 带超时的异步执行
     */
    public static <T> T executeWithTimeout(Callable<T> task, long timeout, TimeUnit unit) {
        Future<T> future = VIRTUAL_EXECUTOR.submit(TtlCallable.get(task));
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            log.error("任务执行超时: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 延迟执行任务
     */
    public static void executeDelayed(Runnable task, long delay, TimeUnit unit) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            VIRTUAL_EXECUTOR.submit(TtlRunnable.get(task));
        }, delay, unit);
        scheduler.shutdown();
    }

    /**
     * 创建虚拟线程执行器
     */
    public static ExecutorService getExecutor() {
        return VIRTUAL_EXECUTOR;
    }
}