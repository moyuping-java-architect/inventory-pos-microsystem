package com.psi.common.trace.feign;

import com.psi.common.context.UserContext;
import com.psi.common.trace.config.TraceProperties;
import com.psi.common.trace.util.TraceUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign调用追踪拦截器
 * 在Feign调用时传递追踪上下文
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TraceFeignInterceptor implements RequestInterceptor {

    private final TraceProperties properties;

    @Override
    public void apply(RequestTemplate template) {
        if (!properties.isEnabled()) {
            return;
        }

        String traceId = UserContext.getTraceId();
        if (traceId != null && !traceId.isEmpty()) {
            String spanId = TraceUtil.generateSpanId();
            template.header(properties.getTraceIdHeader(), traceId);
            template.header(properties.getSpanIdHeader(), spanId);

            String logPrefix = TraceUtil.buildLogPrefix(traceId, properties.getServiceName(), spanId);
            log.debug("{} Feign request tracing headers set: traceId={}, spanId={}",
                    logPrefix, traceId, spanId);
        }
    }
}