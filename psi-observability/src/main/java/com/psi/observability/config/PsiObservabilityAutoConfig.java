package com.psi.observability.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * PSI 可观测性自动装配入口.
 *
 * <p>业务服务只需引入本模块并加 {@code @Import(PsiObservabilityAutoConfig.class)}，
 * 即可开启 {@link com.psi.observability.aspect.PsiTraceAspect} 切面。</p>
 */
@Configuration
@ComponentScan("com.psi.observability")
public class PsiObservabilityAutoConfig {
}
