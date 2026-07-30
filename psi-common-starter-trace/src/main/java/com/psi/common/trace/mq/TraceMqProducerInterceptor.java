package com.psi.common.trace.mq;

import com.psi.common.context.UserContext;
import com.psi.common.trace.config.TraceProperties;
import com.psi.common.trace.util.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * MQ生产者追踪拦截器
 * 在发送消息时传递追踪上下文
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TraceMqProducerInterceptor implements MessagePostProcessor {

    private final TraceProperties properties;

    @Override
    public Message postProcessMessage(Message message) {
        if (!properties.isEnabled()) {
            return message;
        }

        String traceId = UserContext.getTraceId();
        if (traceId != null && !traceId.isEmpty()) {
            String spanId = TraceUtil.generateSpanId();
            message.getMessageProperties().getHeaders().put(
                    properties.getTraceIdHeader(), traceId);
            message.getMessageProperties().getHeaders().put(
                    properties.getSpanIdHeader(), spanId);

            String logPrefix = TraceUtil.buildLogPrefix(traceId, properties.getServiceName(), spanId);
            log.debug("{} MQ message tracing headers set: traceId={}, spanId={}",
                    logPrefix, traceId, spanId);
        }

        return message;
    }
}