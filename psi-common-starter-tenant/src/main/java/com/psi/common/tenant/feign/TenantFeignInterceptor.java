package com.psi.common.tenant.feign;

import com.psi.common.constant.TenantMdcConstant;
import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign租户上下文拦截器
 * 
 * <p>自动拦截所有Feign请求，将租户上下文信息写入HTTP请求头，
 * 实现跨微服务的租户上下文透传。
 * 
 * <p>工作原理：
 * <ol>
 *   <li>从UserContext获取当前租户信息</li>
 *   <li>将租户信息写入HTTP请求头</li>
 *   <li>下游服务通过TenantContextWebInterceptor接收并设置上下文</li>
 * </ol>
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
public class TenantFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        try {
            UserInfo userInfo = UserContext.get();
            
            if (userInfo == null) {
                log.debug("No user context found, skipping Feign header injection");
                return;
            }

            // 将租户信息写入请求头
            writeTenantHeaders(template, userInfo);
            
            log.debug("Tenant context propagated via Feign: tenantId={}, userId={}", 
                    userInfo.getTenantId(), userInfo.getUpdateUserId());
                    
        } catch (Exception e) {
            log.warn("Failed to propagate tenant context via Feign", e);
        }
    }

    /**
     * 将UserInfo中的租户信息写入Feign请求头
     */
    private void writeTenantHeaders(RequestTemplate template, UserInfo userInfo) {
        // 租户ID（必填）
        template.header(TenantMdcConstant.HEADER_TENANT_ID, nullToEmpty(userInfo.getTenantId()));
        
        // 用户信息
        template.header(TenantMdcConstant.HEADER_UPDATE_USER_ID, nullToEmpty(userInfo.getUpdateUserId()));
        template.header(TenantMdcConstant.HEADER_UPDATE_USER_NAME, nullToEmpty(userInfo.getUpdateUserName()));
        
        // 业务维度信息
        template.header(TenantMdcConstant.HEADER_SHOP_ID, nullToEmpty(userInfo.getShopId()));
        template.header(TenantMdcConstant.HEADER_WAREHOUSE_ID, nullToEmpty(userInfo.getWarehouseId()));
        
        // 权限信息
        template.header(TenantMdcConstant.HEADER_ROLE_ID, nullToEmpty(userInfo.getRoleId()));
        template.header(TenantMdcConstant.HEADER_ROLE_NAME, nullToEmpty(userInfo.getRoleName()));
        template.header(TenantMdcConstant.HEADER_PERMISSIONS, nullToEmpty(userInfo.getPermissions()));
    }

    /**
     * 空值处理：null转空字符串
     */
    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}