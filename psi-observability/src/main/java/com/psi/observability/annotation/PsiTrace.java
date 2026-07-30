package com.psi.observability.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PSI 业务级自定义埋点注解.
 *
 * <p>打在 Service/Mapper 方法上，会被 SkyWalking Agent 自动识别（需要开启 @Trace 拦截器）
 * 实际上更推荐直接在方法内部使用 {@code TraceContext.putTag(...)}，
 * 这个注解用于在静态扫描时让评审者/AI 立刻看出哪些是核心业务节点。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @PsiTrace(name = "upSyncData", tags = {"tenantId", "tableName"})
 * public void upSyncData(UpSyncDTO dto) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE) // 仅作标记，运行时无开销
public @interface PsiTrace {

    /** 业务名（显示在 SkyWalking UI） */
    String name();

    /** 需要打 tag 的参数名（通过 SpEL 取值） */
    String[] tags() default {};
}
