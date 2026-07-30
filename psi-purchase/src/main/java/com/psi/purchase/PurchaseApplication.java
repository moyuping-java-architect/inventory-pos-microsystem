package com.psi.purchase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.psi.common.feign"})
public class PurchaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(PurchaseApplication.class, args);
    }
}