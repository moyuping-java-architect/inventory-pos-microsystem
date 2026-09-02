package com.psi.cashier.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;

/**
 * 安全数据源配置
 * 1. 动态解析数据库路径（优先使用环境变量 PSI_DB_PATH）
 * 2. 确保数据库文件存放在安全位置
 * 3. 启动后锁定数据库文件（设置只读权限，防止外部工具修改）
 */
@Slf4j
@Configuration
public class SecureDataSourceConfig {

    @Value("${psi.db.path:}")
    private String configuredDbPath;

    @Value("${psi.db.secure:true}")
    private boolean secureEnabled;

    public void setConfiguredDbPath(String configuredDbPath) {
        this.configuredDbPath = configuredDbPath;
    }

    public void setSecureEnabled(boolean secureEnabled) {
        this.secureEnabled = secureEnabled;
    }

    /**
     * 解析实际的数据库路径
     * 优先级：环境变量 PSI_DB_PATH > 配置 psi.db.path > 默认 data/myp.db
     */
    public String resolveDbPath() {
        // 1. 环境变量优先
        String envPath = System.getenv("PSI_DB_PATH");
        if (envPath != null && !envPath.isEmpty()) {
            log.info("使用环境变量 PSI_DB_PATH: {}", envPath);
            return envPath;
        }

        // 2. 系统属性
        String sysProp = System.getProperty("psi.db.resolved-path");
        if (sysProp != null && !sysProp.isEmpty()) {
            log.info("使用系统属性 psi.db.resolved-path: {}", sysProp);
            return sysProp;
        }

        // 3. 配置文件
        if (configuredDbPath != null && !configuredDbPath.isEmpty()) {
            log.info("使用配置 psi.db.path: {}", configuredDbPath);
            return configuredDbPath;
        }

        // 4. 默认
        String defaultPath = "data/myp.db";
        log.info("使用默认数据库路径: {}", defaultPath);
        return defaultPath;
    }

    /**
     * 启动后锁定数据库文件（设置只读，防止外部工具修改）
     * 仅 Windows 生效
     */
    public void lockDbFile() {
        if (!secureEnabled) return;

        try {
            String dbPath = resolveDbPath();
            File dbFile = new File(dbPath);
            if (dbFile.exists()) {
                // Windows: 设置文件为隐藏+只读（收银员无法直接修改）
                if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                    ProcessBuilder pb = new ProcessBuilder("attrib", "+R", dbFile.getAbsolutePath());
                    pb.start().waitFor();
                    log.info("数据库文件已锁定（只读）: {}", dbPath);
                }
            }
        } catch (Exception e) {
            log.warn("数据库文件锁定失败: {}", e.getMessage());
        }
    }
}
