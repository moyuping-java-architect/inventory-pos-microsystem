package com.psi.common.trace.aspectj;

import com.psi.common.trace.config.TraceProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AspectJ LTW 配置器
 * 在 Spring 容器初始化后，将配置属性注入到 AspectJ 切面中
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TraceLtwConfigurer {

    private final TraceProperties properties;

    @PostConstruct
    public void init() {
        // 将配置属性设置到 AspectJ 切面中
        TraceLtwAspect.setProperties(properties);
        log.info("AspectJ LTW trace aspect initialized with properties: enabled={}, timeoutThreshold={}ms", 
                properties.isEnabled(), properties.getMethodTimeoutThreshold());
    }
}