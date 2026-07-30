package com.psi.order.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 订单规则自动配置类
 * 提供通用单据能力，支持采购订单、销售订单、报损单、报溢单、盘点单等
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.psi.order")
@MapperScan("com.psi.order.mapper")
@EnableConfigurationProperties
public class OrderRuleAutoConfiguration {

}