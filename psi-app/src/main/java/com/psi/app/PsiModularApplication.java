package com.psi.app;

import com.psi.common.mq.autoconfigure.MQAutoConfiguration;
import com.psi.common.mq.autoconfigure.RabbitBindingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {
        "com.psi.app",
        "com.psi.common",
        "com.psi.order",
        "com.psi.system",
        "com.psi.goods",
        "com.psi.sale",
        "com.psi.purchase",
        "com.psi.stock",
        "com.psi.finance",
        "com.psi.member",
        "com.psi.customer",
        "com.psi.message",
        "com.psi.flow",
        "com.psi.sync",
        "com.psi.report",
        "com.psi.observability"
}, exclude = {
        RabbitAutoConfiguration.class,
        RabbitBindingAutoConfiguration.class,
        MQAutoConfiguration.class
})
@MapperScan({"com.psi.system.mapper", "com.psi.goods.mapper", "com.psi.sale.mapper", "com.psi.purchase.mapper", "com.psi.stock.mapper", "com.psi.finance.mapper", "com.psi.member.mapper", "com.psi.customer.mapper", "com.psi.message.mapper", "com.psi.flow.mapper", "com.psi.sync.mapper", "com.psi.order.mapper"})
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties
@Import({
        com.psi.observability.config.PsiObservabilityAutoConfig.class,
        com.psi.common.doc.config.OpenApiAutoConfig.class,
        com.psi.app.config.MQStubConfig.class
})
public class PsiModularApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsiModularApplication.class, args);
    }
}