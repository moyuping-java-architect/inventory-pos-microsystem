package com.psi.common.tenant.config;

import com.psi.common.tenant.web.TenantContextWebInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 注册租户上下文拦截器
 * 
 * @author PSI
 * @version 1.0.0
 */
public class TenantWebMvcConfig implements WebMvcConfigurer {

    private final TenantContextWebInterceptor tenantContextWebInterceptor;

    public TenantWebMvcConfig(TenantContextWebInterceptor tenantContextWebInterceptor) {
        this.tenantContextWebInterceptor = tenantContextWebInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantContextWebInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/health/**", "/error/**");
    }
}