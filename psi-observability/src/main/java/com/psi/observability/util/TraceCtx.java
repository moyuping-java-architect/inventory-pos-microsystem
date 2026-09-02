package com.psi.observability.util;

import com.psi.observability.constant.PsiTags;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

/**
 * PSI 链路追踪上下文工具.
 *
 * <p>封装 SkyWalking {@code apm-toolkit-trace}，
 * 业务代码可以直接用 {@code TraceContext.putTag(...)}，
 * 无需关心 SkyWalking 是否在运行（Agent 未挂载时调用是 no-op）。</p>
 *
 * <h3>使用示例（不依赖 SkyWalking Agent 也能编译）</h3>
 * <pre>{@code
 * public void upSyncData(UpSyncDTO dto) {
 *     TraceCtx.putTag(PsiTags.TENANT_ID, dto.getTenantId());
 *     TraceCtx.putTag(PsiTags.SYNC_TABLE, dto.getTableName());
 *     TraceCtx.putTag(PsiTags.SYNC_KEY, dto.getBusinessKey());
 *
 *     try {
 *         // ... 业务逻辑
 *         TraceCtx.putTag(PsiTags.SYNC_RESULT, "SUCCESS");
 *     } catch (Exception e) {
 *         TraceCtx.putTag(PsiTags.SYNC_RESULT, "FAIL");
 *         TraceCtx.putTag("psi.error", e.getMessage());
 *         throw e;
 *     }
 * }
 * }</pre>
 */
@Slf4j
public final class TraceCtx {

    private TraceCtx() {}

    /**
     * 在当前 span 上打 tag（KV 对，会显示在 SkyWalking UI 的 span 详情里）.
     *
     * @param key   tag 名（建议使用 {@link PsiTags} 常量）
     * @param value tag 值
     */
    public static void putTag(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        try {
            ActiveSpan.tag(key, value);
        } catch (Throwable t) {
            // Agent 未挂载时是 no-op，但不能影响业务
            log.debug("putTag no-op, key={}, value={}", key, value);
        }
    }

    /**
     * 在当前 span 上打 tag（自动 toString）.
     */
    public static void putTag(String key, Object value) {
        putTag(key, value == null ? null : value.toString());
    }

    /**
     * 标记当前 span 发生了错误（SkyWalking 会用红色显示）.
     */
    public static void error(String message) {
        try {
            ActiveSpan.error(new RuntimeException(message));
        } catch (Throwable ignored) {
        }
    }

    /**
     * 标记当前 span 发生了错误（带堆栈）.
     */
    public static void error(Throwable t) {
        if (t == null) return;
        try {
            ActiveSpan.error(t);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 拿到当前 traceId（用于日志关联 / 返回给前端排查）.
     */
    public static String currentTraceId() {
        try {
            return TraceContext.traceId();
        } catch (Throwable t) {
            return "NO-AGENT";
        }
    }

    /**
     * 拿到当前 segmentId（同上）.
     */
    public static String currentSegmentId() {
        try {
            return TraceContext.segmentId();
        } catch (Throwable t) {
            return "NO-AGENT";
        }
    }
}
