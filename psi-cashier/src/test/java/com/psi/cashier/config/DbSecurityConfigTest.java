package com.psi.cashier.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DbSecurityConfig & SecureDataSourceConfig 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DbSecurityConfigTest {

    @Test
    void resolveDbPath_shouldReturnDefaultWhenNotConfigured() {
        SecureDataSourceConfig config = new SecureDataSourceConfig();
        config.setConfiguredDbPath("");
        
        String path = config.resolveDbPath();
        assert "data/myp.db".equals(path) : "默认路径应为 data/myp.db";
    }

    @Test
    void resolveDbPath_shouldReturnConfiguredPath() {
        SecureDataSourceConfig config = new SecureDataSourceConfig();
        config.setConfiguredDbPath("custom/path/db.sqlite");
        
        String path = config.resolveDbPath();
        assert "custom/path/db.sqlite".equals(path) : "应返回配置的路径";
    }

    @Test
    void resolveDbPath_shouldReturnEnvPath() {
        System.setProperty("psi.db.resolved-path", "env/path/db.sqlite");
        
        SecureDataSourceConfig config = new SecureDataSourceConfig();
        config.setConfiguredDbPath("");
        
        String path = config.resolveDbPath();
        assert "env/path/db.sqlite".equals(path) : "应返回环境变量路径";
        
        System.clearProperty("psi.db.resolved-path");
    }

    @Test
    void lockDbFile_shouldNotThrowWhenSecureDisabled() {
        SecureDataSourceConfig config = new SecureDataSourceConfig();
        config.setConfiguredDbPath("");
        config.setSecureEnabled(false);
        
        config.lockDbFile();
    }
}
