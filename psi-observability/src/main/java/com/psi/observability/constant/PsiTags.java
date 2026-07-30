package com.psi.observability.constant;

/**
 * PSI 自定义 SkyWalking 标签（Tag）常量.
 *
 * <p>统一管理所有自定义 tag 名称，避免散落字符串拼写错误。
 * 在 SkyWalking UI 中显示在 span 详情里，可以按这些 tag 过滤/搜索。</p>
 */
public final class PsiTags {

    private PsiTags() {}

    // ============ 业务通用 ============
    public static final String TENANT_ID    = "psi.tenant_id";
    public static final String STORE_ID     = "psi.store_id";
    public static final String TERMINAL_ID  = "psi.terminal_id";
    public static final String USER_ID      = "psi.user_id";

    // ============ 同步模块 ============
    public static final String SYNC_TABLE   = "psi.sync.table";
    public static final String SYNC_KEY     = "psi.sync.business_key";
    public static final String SYNC_VERSION = "psi.sync.version";
    public static final String SYNC_ACTION  = "psi.sync.action";        // INSERT/UPDATE/SKIP/CONFLICT
    public static final String SYNC_RETRY   = "psi.sync.retry_count";
    public static final String SYNC_RESULT  = "psi.sync.result";        // SUCCESS/FAIL

    // ============ 流程模块 ============
    public static final String FLOW_BIZ_ID  = "psi.flow.biz_id";
    public static final String FLOW_DOC_NO  = "psi.flow.doc_no";
    public static final String FLOW_DOC_TYP = "psi.flow.doc_type";

    // ============ 幂等 ============
    public static final String IDEMPOTENT_KEY = "psi.idempotent.key";
    public static final String IDEMPOTENT_HIT = "psi.idempotent.hit";   // true=命中
}
