package com.trademaster.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.trademaster.mapper", "com.trademaster.workflow.mapper"})
public class MyBatisPlusConfig {
}
