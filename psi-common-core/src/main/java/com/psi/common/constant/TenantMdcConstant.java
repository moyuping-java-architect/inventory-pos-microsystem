package com.psi.common.constant;

/**
 * 租户MDC常量,请求头常量
 * 用于在日志中记录租户ID
 *
 */
public class TenantMdcConstant {
    //MDC常量 KEY
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_TENANT_ID = "tenantId";
    public static final String MDC_SHOP_ID = "shopId";
    public static final String MDC_WAREHOUSE_ID = "warehouseId";
    public static final String MDC_UPDATE_USER_ID = "updateUserId";
    public static final String MDC_UPDATE_USER_NAME = "updateUserName";
    public static final String MDC_ROLE_ID = "roleId";
    public static final String MDC_ROLE_NAME = "roleName";
    public static final String MDC_PERMISSIONS = "permissions";

    //请求头常量 KEY
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_SHOP_ID = "X-Shop-Id";
    public static final String HEADER_WAREHOUSE_ID = "X-Warehouse-Id";
    public static final String HEADER_UPDATE_USER_ID = "X-Update-User-Id";
    public static final String HEADER_UPDATE_USER_NAME = "X-Update-User-Name";
    public static final String HEADER_ROLE_ID = "X-Role-Id";
    public static final String HEADER_ROLE_CODE = "X-Role-Code";
    public static final String HEADER_ROLE_NAME = "X-Role-Name";
    public static final String HEADER_PERMISSIONS = "X-Permissions";






}