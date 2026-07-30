package com.psi.system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Validation Controller
 * Used to verify if Nacos configuration is loaded correctly
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    @Value("${config.source:unknown}")
    private String configSource;

    @Value("${spring.datasource.url:not-set}")
    private String datasourceUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.application.name:not-set}")
    private String appName;

    /**
     * Get current configuration information
     */
    @GetMapping("/info")
    public Map<String, Object> getConfigInfo() {
        Map<String, Object> config = new HashMap<>();
        config.put("configSource", configSource);
        config.put("datasourceUrl", datasourceUrl);
        config.put("serverPort", serverPort);
        config.put("appName", appName);
        
        // Check if Nacos configuration is loaded
        boolean isNacosConfigured = "nacos".equals(configSource);
        config.put("nacosConfigLoaded", isNacosConfigured);
        
        return config;
    }
}