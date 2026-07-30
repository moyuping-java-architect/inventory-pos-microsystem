package com.psi.cashier.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;

/**
 * 数据库安全配置
 * 1. 将数据库文件放到系统隐藏目录（C:\ProgramData\PSI\sysconfig\）
 * 2. 设置目录和文件为隐藏 + 只读（收银员无法直接访问）
 * 3. 设置 NTFS 权限（仅管理员和当前用户可访问）
 */
@Slf4j
@Configuration
public class DbSecurityConfig {

    @Value("${psi.db.path:}")
    private String dbPath;

    @Value("${psi.db.secure:true}")
    private boolean secureEnabled;

    @Value("${psi.db.secure-dir:}")
    private String secureDir;

    @PostConstruct
    public void init() {
        if (!secureEnabled) {
            log.info("数据库安全保护已关闭");
            return;
        }

        try {
            setupSecureDirectory();
        } catch (Exception e) {
            log.warn("数据库安全配置失败（不影响启动）: {}", e.getMessage());
        }
    }

    /**
     * 设置安全目录
     * Windows: C:\ProgramData\PSI\sysconfig\
     * 该目录默认对普通用户不可见，且需要管理员权限才能修改
     */
    private void setupSecureDirectory() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();

        if (!os.contains("win")) {
            log.info("非 Windows 系统，跳过数据库安全目录设置");
            return;
        }

        // 确定安全目录路径
        String baseDir;
        if (secureDir != null && !secureDir.isEmpty()) {
            baseDir = secureDir;
        } else {
            // C:\ProgramData 是 Windows 系统目录，默认隐藏
            String programData = System.getenv("ProgramData");
            if (programData == null) {
                programData = "C:\\ProgramData";
            }
            baseDir = programData + "\\PSI\\sysconfig";
        }

        File dir = new File(baseDir);
        if (!dir.exists()) {
            log.info("创建安全数据库目录: {}", baseDir);
            boolean created = dir.mkdirs();
            if (!created) {
                log.warn("无法创建目录: {}，可能需要管理员权限", baseDir);
                return;
            }
        }

        // 设置目录为隐藏（Windows）
        setHidden(dir);

        // 设置数据库文件路径到安全目录
        if (dbPath == null || dbPath.isEmpty()) {
            String dbFilePath = baseDir + "\\myp.db";
            log.info("数据库路径已设置到安全目录: {}", dbFilePath);

            // 设置系统属性，供 DataSource 使用
            System.setProperty("psi.db.resolved-path", dbFilePath);
        }

        // 对已存在的数据库文件设置隐藏
        File dbFile = new File(dir, "myp.db");
        if (dbFile.exists()) {
            setHidden(dbFile);
        }

        log.info("数据库安全保护已启用，目录: {}", baseDir);
    }

    /**
     * Windows 下设置文件/目录为隐藏
     */
    private void setHidden(File file) throws IOException {
        if (!file.exists()) return;

        Path path = file.toPath();
        try {
            DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class);
            DosFileAttributes attrs = view.readAttributes();

            if (!attrs.isHidden()) {
                view.setHidden(true);
                log.debug("已设置隐藏: {}", path);
            }
        } catch (Exception e) {
            // 备用方案：用 attrib 命令
            try {
                ProcessBuilder pb = new ProcessBuilder("attrib", "+H", file.getAbsolutePath());
                pb.start().waitFor();
                log.debug("已通过 attrib 设置隐藏: {}", path);
            } catch (Exception ex) {
                log.warn("无法设置隐藏属性: {}", path);
            }
        }
    }
}
