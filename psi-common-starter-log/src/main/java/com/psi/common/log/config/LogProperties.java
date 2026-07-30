package com.psi.common.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志统一配置属性
 */
@Data
@ConfigurationProperties(prefix = "psi.log")
public class LogProperties {

    /**
     * 是否启用统一日志配置
     */
    private boolean enabled = true;

    /**
     * 统一日志根目录
     */
    private String baseDir = "./logs";

    /**
     * 服务名称（用于创建服务子目录）
     */
    private String serviceName;

    /**
     * 日志级别
     */
    private String level = "INFO";

    /**
     * 是否输出到控制台
     */
    private boolean consoleEnabled = true;

    /**
     * 是否输出到文件
     */
    private boolean fileEnabled = true;

    /**
     * 文件日志保留天数
     */
    private int retentionDays = 7;

    /**
     * 单个日志文件大小（MB）
     */
    private int maxFileSizeMb = 100;

    /**
     * 每日日志文件数量限制
     */
    private int maxFilesPerDay = 10;

    /**
     * 慢日志阈值（毫秒）
     */
    private long slowLogThresholdMs = 3000;

    /**
     * 是否启用异步日志
     */
    private boolean asyncEnabled = true;

    /**
     * 异步队列大小
     */
    private int asyncQueueSize = 1024;
}