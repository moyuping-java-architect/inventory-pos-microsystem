package com.psi.common.nacos.autoconfigure;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@AutoConfiguration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.cloud.nacos.discovery.enabled", havingValue = "true", matchIfMissing = true)
@EnableDiscoveryClient
public class NacosAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("[PSI-Nacos-Starter] 初始化成功 ✅ SpringBoot3.3.10 专用");
    }
}