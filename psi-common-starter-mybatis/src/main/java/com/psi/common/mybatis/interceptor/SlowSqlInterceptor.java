package com.psi.common.mybatis.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * MyBatis 慢SQL拦截器
 * 自动记录执行时间超过阈值的SQL
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "query", 
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", 
               args = {MappedStatement.class, Object.class})
})
public class SlowSqlInterceptor implements Interceptor {

    /**
     * 慢SQL阈值（毫秒）
     */
    private long thresholdMs = 500L;

    /**
     * 是否显示SQL
     */
    private boolean showSql = true;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        
        String sqlId = ms.getId();
        String operationType = ms.getSqlCommandType().name();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行原始方法
            return invocation.proceed();
        } finally {
            // 计算执行时间
            long executeTime = System.currentTimeMillis() - startTime;
            
            // 判断是否为慢SQL
            if (executeTime >= thresholdMs) {
                StringBuilder sb = new StringBuilder();
                sb.append("[Slow SQL] ");
                sb.append("sqlId=").append(sqlId).append(", ");
                sb.append("operation=").append(operationType).append(", ");
                sb.append("executeTime=").append(executeTime).append("ms");
                
                // 获取SQL
                if (showSql) {
                    Object parameter = args.length > 1 ? args[1] : null;
                    BoundSql boundSql = ms.getBoundSql(parameter);
                    String sql = boundSql.getSql();
                    if (sql != null && !sql.isEmpty()) {
                        sb.append(", sql=");
                        sb.append(sql.length() > 500 ? sql.substring(0, 500) + "..." : sql);
                    }
                }
                
                log.warn(sb.toString());
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 从配置中读取阈值
        String threshold = properties.getProperty("thresholdMs");
        if (threshold != null && !threshold.isEmpty()) {
            this.thresholdMs = Long.parseLong(threshold);
        }
        
        // 从配置中读取是否显示SQL
        String showSqlProp = properties.getProperty("showSql");
        if (showSqlProp != null && !showSqlProp.isEmpty()) {
            this.showSql = Boolean.parseBoolean(showSqlProp);
        }
    }

    public void setThresholdMs(long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }
}