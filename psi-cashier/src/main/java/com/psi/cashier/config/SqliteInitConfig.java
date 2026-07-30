package com.psi.cashier.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.annotation.PostConstruct;

/**
 * SQLite 数据库自动初始化配置
 * 首次启动时自动执行 DB/migration/ 下的 SQL 脚本创建表结构
 * 后续启动时跳过已执行过的脚本（通过 flyway_schema_history 表记录）
 */
@Slf4j
@Configuration
public class SqliteInitConfig {

    private final DataSource dataSource;

    @Value("${psi.db.auto-init:true}")
    private boolean autoInit;

    public SqliteInitConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        if (!autoInit) {
            log.info("数据库自动初始化已关闭");
            return;
        }

        try {
            ensureSchemaHistoryTable();
            executePendingMigrations();
        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /**
     * 确保 flyway_schema_history 表存在
     */
    private void ensureSchemaHistoryTable() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='flyway_schema_history'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        log.info("创建 flyway_schema_history 表...");
                        try (PreparedStatement createPs = conn.prepareStatement(
                                "CREATE TABLE flyway_schema_history (" +
                                        "installed_rank INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                        "version VARCHAR(50) NOT NULL, " +
                                        "description VARCHAR(200), " +
                                        "script VARCHAR(1000), " +
                                        "installed_on VARCHAR(20), " +
                                        "execution_time INTEGER DEFAULT 0, " +
                                        "success INTEGER DEFAULT 1)")) {
                            createPs.executeUpdate();
                        }
                        log.info("flyway_schema_history 表创建成功");
                    }
                }
            }
        }
    }

    /**
     * 执行未执行的迁移脚本
     */
    private void executePendingMigrations() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:DB/migration/V*.sql");

        // 收集并排序迁移脚本
        List<Resource> sortedResources = new ArrayList<>(List.of(resources));
        sortedResources.sort(Comparator.comparing(r -> r.getFilename() != null ? r.getFilename() : ""));

        // 查询已执行的版本
        List<String> executedVersions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT version FROM flyway_schema_history WHERE success = 1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                executedVersions.add(rs.getString("version"));
            }
        }

        // 执行未执行的脚本
        for (Resource resource : sortedResources) {
            String filename = resource.getFilename();
            if (filename == null) continue;

            String version = extractVersion(filename);
            if (version == null || executedVersions.contains(version)) {
                log.debug("跳过已执行的迁移: {}", filename);
                continue;
            }

            log.info("执行数据库迁移: {}", filename);
            long startTime = System.currentTimeMillis();

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(resource);
            populator.setSeparator(";");
            populator.setCommentPrefix("--");
            populator.execute(dataSource);

            long elapsed = System.currentTimeMillis() - startTime;

            // 记录执行结果
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO flyway_schema_history (version, description, script, installed_on, execution_time, success) " +
                                 "VALUES (?, ?, ?, datetime('now'), ?, 1)")) {
                ps.setString(1, version);
                ps.setString(2, filename);
                ps.setString(3, filename);
                ps.setLong(4, elapsed);
                ps.executeUpdate();
            }

            log.info("数据库迁移完成: {} ({}ms)", filename, elapsed);
        }

        if (sortedResources.isEmpty()) {
            log.info("没有找到数据库迁移脚本");
        }
    }

    /**
     * 从文件名提取版本号
     * 如 V1__INIT.sql → 1, V2__CREATE_TABLE.sql → 2
     */
    private String extractVersion(String filename) {
        if (!filename.startsWith("V") || !filename.contains("__")) {
            return null;
        }
        try {
            String versionPart = filename.substring(1, filename.indexOf("__"));
            return versionPart;
        } catch (Exception e) {
            log.warn("无法解析版本号: {}", filename);
            return null;
        }
    }
}
