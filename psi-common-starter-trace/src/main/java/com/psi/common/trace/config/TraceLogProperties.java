package com.psi.common.trace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志查询配置属性
 */
@Data
@ConfigurationProperties(prefix = "psi.trace.log")
public class TraceLogProperties {

    /**
     * 日志目录
     */
    private String dir = "./logs";

    /**
     * 上下文行数（匹配行后显示的行数）
     */
    private int contextLines = 10;

    /**
     * 前置上下文行数
     */
    private int beforeLines = 2;
}