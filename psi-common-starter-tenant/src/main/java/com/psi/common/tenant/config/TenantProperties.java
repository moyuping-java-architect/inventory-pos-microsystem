package com.psi.common.tenant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 租户配置属性类
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "psi.tenant")
public class TenantProperties {

    /**
     * 是否启用租户上下文
     */
    private boolean enabled = true;

    /**
     * 是否强制要求租户上下文
     */
    private boolean required = false;

    /**
     * 默认租户ID（当上下文为空时使用）
     */
    private String defaultTenantId = "1";

    /**
     * 请求头中租户ID的键名
     */
    private String tenantIdHeader = "X-Tenant-Id";

    /**
     * 请求头中用户ID的键名
     */
    private String userIdHeader = "X-User-Id";

    /**
     * 请求头中店铺ID的键名
     */
    private String shopIdHeader = "X-Shop-Id";

    /**
     * 请求头中仓库ID的键名
     */
    private String warehouseIdHeader = "X-Warehouse-Id";

    /**
     * 请求头中用户名的键名
     */
    private String userNameHeader = "X-User-Name";

    /**
     * 请求头中角色ID的键名
     */
    private String roleIdHeader = "X-Role-Id";

    /**
     * 请求头中角色名称的键名
     */
    private String roleNameHeader = "X-Role-Name";

    /**
     * 请求头中权限的键名
     */
    private String permissionsHeader = "X-Permissions";

    /**
     * 是否在MDC中记录租户信息
     */
    private boolean mdcEnabled = true;
}