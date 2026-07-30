package com.psi.common.mybatis.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.psi.common.mybatis.handler.EntityMetaObjectHandler;
import com.psi.common.mybatis.interceptor.SlowSqlInterceptor;
import com.psi.common.mybatis.interceptor.TenantInterceptor;
import com.psi.common.mybatis.properties.MyBatisProperties;
import com.psi.common.mybatis.util.BatchUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis 自动配置类
 * 配置分页插件和自动填充处理器
 * 
 * Spring Boot 3.x 自动配置方式
 *
 * @author PSI
 * @version 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(MyBatisProperties.class)
@RequiredArgsConstructor
public class MyBatisAutoConfiguration {

    private final MyBatisProperties properties;

    /**
     * 配置分页插件
     * 
     * @return PaginationInnerInterceptor 实例
     */
    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        
        // 设置数据库类型为 SQLite（支持 SQLite 的 LIMIT OFFSET 语法）
        // SQLite 使用: SELECT * FROM table LIMIT size OFFSET offset
        paginationInterceptor.setDbType(DbType.SQLITE);
        
        // 开启 count 的 join 优化
        paginationInterceptor.setOptimizeJoin(true);
        
        // 设置最大单页限制数量
        paginationInterceptor.setMaxLimit(properties.getPagination().getMaxLimit());
        
        // 开启分页合理化（页码<=0 查询第一页，页码>=总页数查询最后一页）
        paginationInterceptor.setOverflow(false);
        
        return paginationInterceptor;
    }

    /**
     * 配置租户拦截器
     * 
     * @return TenantInterceptor 实例
     */
    @Bean
    public TenantInterceptor tenantInterceptor() {
        return new TenantInterceptor(properties);
    }

    /**
     * 配置慢SQL拦截器（传统 MyBatis 拦截器）
     * 
     * @return SlowSqlInterceptor 实例
     */
    @Bean
    public SlowSqlInterceptor slowSqlInterceptor() {
        SlowSqlInterceptor interceptor = new SlowSqlInterceptor();
        interceptor.setThresholdMs(properties.getSlowSql().getThresholdMs());
        interceptor.setShowSql(properties.getSlowSql().isShowSql());
        return interceptor;
    }

    /**
     * 配置 MyBatis-Plus 拦截器
     * 包含分页插件和租户拦截器
     * 注意：慢SQL拦截器使用传统方式注册，不在此添加
     *
     * 执行顺序：
     * 1. 租户拦截器（TenantInterceptor）- 先添加租户条件
     * 2. 分页拦截器（PaginationInnerInterceptor）- 再处理分页和 COUNT
     *    必须让租户拦截器先于分页执行，否则分页插件内部 COUNT 查询不会携带租户条件，
     *    导致 total 与 records 不一致。
     *
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            PaginationInnerInterceptor paginationInterceptor,
            TenantInterceptor tenantInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 先添加租户拦截器，确保分页插件内部生成的 COUNT/records SQL 都携带租户条件
        interceptor.addInnerInterceptor(tenantInterceptor);

        // 再添加分页插件
        if (properties.getPagination().isEnabled()) {
            interceptor.addInnerInterceptor(paginationInterceptor);
        }

        return interceptor;
    }

    /**
     * 注册实体字段自动填充处理器
     * 用于自动填充创建时间、更新时间、创建人、更新人等字段
     * 
     * @return EntityMetaObjectHandler 实例
     */
    @Bean
    public EntityMetaObjectHandler entityMetaObjectHandler() {
        return new EntityMetaObjectHandler(properties);
    }

    /**
     * 注册批量操作工具类
     * 支持通过 Nacos 配置管理批次大小
     * 
     * @return BatchUtils 实例
     */
    @Bean
    public BatchUtils batchUtils() {
        return new BatchUtils(properties);
    }
}