package com.psi.system.config;

import com.psi.system.filter.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 使用 DefaultHttpFirewall（宽松模式）替代默认的 StrictHttpFirewall
     * 允许中文等非 ASCII 字符出现在请求头（如 X-Update-User-Name: 超级管理员）
     * 仅保留路径遍历等基础安全检查
     */
    @Bean
    public HttpFirewall httpFirewall() {
        return new DefaultHttpFirewall();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityConfig: Configuring security filter chain - Development mode");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/psi/admin/login").permitAll()
                        .requestMatchers("/psi/admin/refresh-token").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                );

        log.info("SecurityConfig: Security filter chain configured - Development mode");
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterFilterRegistrationBean() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 生产环境：仅允许本地访问（单机版通过 nginx 同源代理，CORS 仅用于直连后端调试场景）
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
