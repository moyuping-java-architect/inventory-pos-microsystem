package com.psi.cashier.config;

import com.psi.cashier.interceptor.CashierAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 收银Web配置
 */
@Configuration
@RequiredArgsConstructor
public class CashierWebConfig implements WebMvcConfigurer {

    private final CashierAuthInterceptor cashierAuthInterceptor;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(0, stringConverter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cashierAuthInterceptor)
                .addPathPatterns("/psi/cashier/**")
                .excludePathPatterns("/psi/cashier/auth/login")
                .excludePathPatterns("/psi/cashier/health")
                .excludePathPatterns("/psi/cashier/static/**")
                .excludePathPatterns("/*.html")
                .excludePathPatterns("/js/**");
    }
}