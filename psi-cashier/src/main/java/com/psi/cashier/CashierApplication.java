package com.psi.cashier;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 收银微服务启动类
 *
 * @author PSI
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.psi.cashier.mapper")
@ServletComponentScan
@EnableFeignClients
public class CashierApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashierApplication.class, args);
    }
}