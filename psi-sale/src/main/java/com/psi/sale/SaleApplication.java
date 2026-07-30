package com.psi.sale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.psi.common.feign"})
public class SaleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaleApplication.class, args);
    }
}
