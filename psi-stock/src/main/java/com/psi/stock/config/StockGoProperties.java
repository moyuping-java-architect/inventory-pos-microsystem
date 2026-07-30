package com.psi.stock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "psi.stock.go")
public class StockGoProperties {

    /**
     * 是否启用 Go 库存服务
     */
    private boolean enabled = false;

    /**
     * Go 库存服务地址
     */
    private String url = "http://localhost:8089";
}
