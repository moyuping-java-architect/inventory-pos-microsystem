package com.psi.common.mybatis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * MyBatis 属性配置
 * 支持 Nacos 动态配置刷新
 * 配置前缀: psi.mybatis
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "psi.mybatis")
@RefreshScope
public class MyBatisProperties {

    /**
     * 分页配置
     */
    private Pagination pagination = new Pagination();

    /**
     * 自动填充配置
     */
    private AutoFill autoFill = new AutoFill();

    /**
     * 租户拦截器配置
     */
    private Tenant tenant = new Tenant();

    /**
     * 慢SQL拦截器配置
     */
    private SlowSql slowSql = new SlowSql();

    /**
     * 批量操作配置
     */
    private Batch batch = new Batch();

    @Data
    public static class SlowSql {
        
        /**
         * 是否启用慢SQL拦截器
         */
        private boolean enabled = true;
        
        /**
         * 慢SQL阈值（毫秒），超过此时间的SQL将被记录
         */
        private long thresholdMs = 500L;
        
        /**
         * 是否打印完整SQL（包含参数值）
         */
        private boolean showSql = true;
    }

    @Data
    public static class Pagination {
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 最大单页限制数量
         */
        private long maxLimit = 500L;
    }

    @Data
    public static class AutoFill {
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 是否填充创建人
         */
        private boolean createBy = true;
        
        /**
         * 是否填充创建时间
         */
        private boolean createTime = true;
        
        /**
         * 是否填充更新人
         */
        private boolean updateBy = true;
        
        /**
         * 是否填充更新时间
         */
        private boolean updateTime = true;
        
        /**
         * 是否填充租户 ID
         */
        private boolean tenantId = true;
        
        /**
         * 是否填充删除标志
         */
        private boolean delFlag = true;
        
        /**
         * 是否填充状态
         */
        private boolean status = true;
    }

    @Data
    public static class Tenant {
        
        /**
         * 是否启用租户拦截器
         */
        private boolean enabled = true;
        
        /**
         * 租户拦截器白名单表名列表
         * 这些表不会被添加租户条件过滤
         */
        private List<String> whitelistTables = List.of(
                "sys_dict_type",
                "sys_dict_data",
                "sys_config",
                "sys_tenant",
                "operator"
        );
    }

    @Data
    public static class Batch {
        
        /**
         * 是否启用批量操作优化
         */
        private boolean enabled = true;
        
        /**
         * 默认批次大小
         * 当具体操作类型未配置时使用
         */
        private int defaultSize = 500;
        
        /**
         * 最大批次大小（全局限制）
         */
        private int maxSize = 1000;
        
        /**
         * 最小批次大小（全局限制）
         */
        private int minSize = 100;
        
        /**
         * 批量保存配置
         */
        private OperationBatch save = new OperationBatch();
        
        /**
         * 批量更新配置
         */
        private OperationBatch update = new OperationBatch();
        
        /**
         * 批量查询配置
         */
        private OperationBatch query = new OperationBatch();
        
        /**
         * 批量删除配置
         */
        private OperationBatch delete = new OperationBatch();
    }
    
    @Data
    public static class OperationBatch {
        
        /**
         * 是否使用独立配置
         */
        private boolean enabled = false;
        
        /**
         * 批次大小
         */
        private int size = 500;
    }
}