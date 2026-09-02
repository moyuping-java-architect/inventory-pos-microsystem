package com.psi.common.log.autoconfigure;

import com.psi.common.log.config.LogProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 日志统一配置自动配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LogProperties.class)
public class LogAutoConfiguration {

    private final LogProperties logProperties;
    private final Environment environment;

    @PostConstruct
    public void init() {
        if (!logProperties.isEnabled()) {
            return;
        }

        // 获取服务名称
        String serviceName = logProperties.getServiceName();
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = environment.getProperty("spring.application.name", "unknown-service");
            logProperties.setServiceName(serviceName);
        }

        // 创建日志目录
        createLogDirectory();

        // 设置系统属性（供logback使用）
        setSystemProperties();

        log.info("══════════════════════════════════════════════════════════════════════");
        log.info("                    日志统一配置初始化完成                              ");
        log.info("══════════════════════════════════════════════════════════════════════");
        log.info("  日志根目录: {}", logProperties.getBaseDir());
        log.info("  服务日志目录: {}/{}", logProperties.getBaseDir(), serviceName);
        log.info("  日志级别: {}", logProperties.getLevel());
        log.info("  控制台输出: {}", logProperties.isConsoleEnabled());
        log.info("  文件输出: {}", logProperties.isFileEnabled());
        log.info("  保留天数: {}天", logProperties.getRetentionDays());
        log.info("══════════════════════════════════════════════════════════════════════");
    }

    private void createLogDirectory() {
        try {
            String serviceName = logProperties.getServiceName();
            Path serviceLogDir = Paths.get(logProperties.getBaseDir(), serviceName);
            
            if (!Files.exists(serviceLogDir)) {
                Files.createDirectories(serviceLogDir);
                log.debug("创建服务日志目录: {}", serviceLogDir.toAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("创建日志目录失败: {}", e.getMessage());
        }
    }

    private void setSystemProperties() {
        String serviceName = logProperties.getServiceName();
        
        // 设置日志根目录
        System.setProperty("psi.log.base-dir", logProperties.getBaseDir());
        
        // 设置服务日志目录
        System.setProperty("psi.log.service-dir", 
                Paths.get(logProperties.getBaseDir(), serviceName).toString());
        
        // 设置服务名称
        System.setProperty("psi.log.service-name", serviceName);
        
        // 设置日志级别
        System.setProperty("psi.log.level", logProperties.getLevel());
        
        // 设置保留天数
        System.setProperty("psi.log.retention-days", String.valueOf(logProperties.getRetentionDays()));
        
        // 设置文件大小限制
        System.setProperty("psi.log.max-file-size", logProperties.getMaxFileSizeMb() + "MB");
        
        // 设置每日文件数量限制
        System.setProperty("psi.log.max-files-per-day", String.valueOf(logProperties.getMaxFilesPerDay()));
        
        // 设置是否启用控制台输出
        System.setProperty("psi.log.console-enabled", String.valueOf(logProperties.isConsoleEnabled()));
        
        // 设置是否启用文件输出
        System.setProperty("psi.log.file-enabled", String.valueOf(logProperties.isFileEnabled()));
        
        // 设置异步日志配置
        System.setProperty("psi.log.async-enabled", String.valueOf(logProperties.isAsyncEnabled()));
        System.setProperty("psi.log.async-queue-size", String.valueOf(logProperties.getAsyncQueueSize()));
    }

    @Bean
    @ConditionalOnMissingBean
    public LogProperties logProperties() {
        return new LogProperties();
    }
}