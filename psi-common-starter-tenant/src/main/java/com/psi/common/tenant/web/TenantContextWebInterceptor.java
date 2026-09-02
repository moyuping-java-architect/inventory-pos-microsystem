package com.psi.common.tenant.web;

import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import com.psi.common.tenant.config.TenantProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * 租户上下文拦截器
 * 在Web请求中自动提取租户信息并设置到UserContext
 * 
 * <p>核心功能：
 * <ul>
 *   <li>从请求头提取租户相关信息</li>
 *   <li>设置到UserContext供全局使用</li>
 *   <li>同步设置MDC用于日志追踪</li>
 *   <li>请求结束后自动清理</li>
 * </ul>
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TenantContextWebInterceptor implements HandlerInterceptor {

    private final TenantProperties properties;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                             @NonNull HttpServletResponse response, 
                             @NonNull Object handler) {
        try {
            // 构建用户信息
            UserInfo userInfo = buildUserInfo(request);
            
            // 设置上下文
            UserContext.set(userInfo);
            log.debug("Tenant context initialized: tenantId={}, userId={}", 
                    userInfo.getTenantId(), userInfo.getUpdateUserId());
                    
        } catch (Exception e) {
            log.warn("Failed to initialize tenant context", e);
            if (properties.isRequired()) {
                throw new IllegalStateException("Tenant context initialization failed", e);
            }
        }
        
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, 
                               @NonNull HttpServletResponse response, 
                               @NonNull Object handler, 
                               Exception ex) {
        // 清理上下文
        clearContext();
    }

    /**
     * 构建用户信息对象
     * 从请求头中提取所有租户相关信息并设置到 UserInfo
     */
    private UserInfo buildUserInfo(HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        
        // 获取租户ID（必填，无值时使用配置的默认租户ID）
        String tenantId = getHeaderValue(request, properties.getTenantIdHeader());
        userInfo.setTenantId(tenantId.isEmpty() ? properties.getDefaultTenantId() : tenantId);
        
        // 获取用户ID
        userInfo.setUpdateUserId(getHeaderValue(request, properties.getUserIdHeader()));
        
        // 获取用户名
        userInfo.setUpdateUserName(getHeaderValue(request, properties.getUserNameHeader()));
        
        // 获取店铺ID
        userInfo.setShopId(getHeaderValue(request, properties.getShopIdHeader()));
        
        // 获取仓库ID
        userInfo.setWarehouseId(getHeaderValue(request, properties.getWarehouseIdHeader()));
        
        // 获取角色ID
        userInfo.setRoleId(getHeaderValue(request, properties.getRoleIdHeader()));
        
        // 获取角色名称
        userInfo.setRoleName(getHeaderValue(request, properties.getRoleNameHeader()));
        
        // 获取权限列表
        userInfo.setPermissions(getHeaderValue(request, properties.getPermissionsHeader()));
        
        return userInfo;
    }
    /**
     * 清理上下文
     */
    private void clearContext() {
        try {
            // 清理UserContext
            UserContext.clearAll();
            
            log.debug("Tenant context cleared");
        } catch (Exception e) {
            log.warn("Failed to clear tenant context", e);
        }
    }

    /**
     * 从请求头获取值，空值返回空字符串
     */
    private String getHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return nullToEmpty(value);
    }

    /**
     * 将null转换为空字符串
     */
    private String nullToEmpty(String value) {
        return Objects.requireNonNullElse(value, "");
    }
}