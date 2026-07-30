package com.psi.sync;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 数据同步中间微服务启动类
 * 
 * @author PSI
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.psi.sync.mapper")
public class SyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncApplication.class, args);
    }
}