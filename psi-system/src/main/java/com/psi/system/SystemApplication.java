package com.psi.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.psi.system.mapper")
@EnableFeignClients(basePackages = {"com.psi.common.feign"})
public class SystemApplication {
    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled", "false");
        System.setProperty("com.alibaba.nacos.logging.default.config.enabled", "false");
        SpringApplication.run(SystemApplication.class, args);
    }
}
