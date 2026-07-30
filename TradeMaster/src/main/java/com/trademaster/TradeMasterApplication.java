package com.trademaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TradeMaster - 赞比亚进销存收银系统（单机版）
 * 
 * 包含三大模块：
 * 1. 收银模块 - POS收银、会员管理、小票打印
 * 2. 进销存模块 - 商品、库存、采购、销售
 * 3. 系统模块 - 用户、权限、系统设置
 */
@SpringBootApplication
public class TradeMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeMasterApplication.class, args);
    }
}
