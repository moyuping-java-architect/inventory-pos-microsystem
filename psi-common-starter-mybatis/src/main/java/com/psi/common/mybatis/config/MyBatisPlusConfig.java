package com.psi.common.mybatis.config;

import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 扩展配置类
 * 提供批量操作支持等增强功能
 * 
 * @author PSI
 * @version 1.0.0
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置 SQL 注入器
     * 支持 insertBatchSomeColumn 等批量操作方法
     * 
     * @return DefaultSqlInjector 实例
     */
    @Bean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }
}