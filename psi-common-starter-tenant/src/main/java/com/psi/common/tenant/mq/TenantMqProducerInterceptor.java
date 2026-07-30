package com.psi.common.tenant.mq;

import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

import java.util.Map;

/**
 * MQ生产者租户上下文拦截器
 * 
 * <p>核心功能：
 * <ul>
 *   <li>从UserContext获取当前租户信息</li>
 *   <li>将租户信息写入MQ消息头</li>
 *   <li>支持租户上下文在MQ消息中透传</li>
 * </ul>
 * 
 * <p>使用方式：
 * <pre>
 * // 在RabbitTemplate中注册
 * rabbitTemplate.setBeforePublishPostProcessors(new TenantMqProducerInterceptor());
 * </pre>
 */
public class TenantMqProducerInterceptor implements MessagePostProcessor {

    private static final Logger log = LoggerFactory.getLogger(TenantMqProducerInterceptor.class);

    /**
     * 消息头常量定义
     */
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_SHOP_ID = "X-Shop-Id";
    private static final String HEADER_WAREHOUSE_ID = "X-Warehouse-Id";
    private static final String HEADER_ROLE_ID = "X-Role-Id";
    private static final String HEADER_ROLE_NAME = "X-Role-Name";
    private static final String HEADER_PERMISSIONS = "X-Permissions";

    @Override
    public Message postProcessMessage(Message message) {
        try {
            // 从UserContext获取用户信息
            UserInfo userInfo = UserContext.get();
            
            if (userInfo == null) {
                log.debug("No user context found, skipping tenant header injection");
                return message;
            }

            // 将租户信息写入消息头
            writeTenantHeaders(message, userInfo);
            
            log.debug("Tenant context propagated: tenantId={}, userId={}", 
                    userInfo.getTenantId(), userInfo.getUpdateUserId());
                    
        } catch (Exception e) {
            log.warn("Failed to propagate tenant context to MQ message", e);
        }
        
        return message;
    }

    /**
     * 将UserInfo中的租户信息写入消息头
     */
    private void writeTenantHeaders(Message message, UserInfo userInfo) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        
        // 租户ID（必填）
        headers.put(HEADER_TENANT_ID, nullToEmpty(userInfo.getTenantId()));
        
        // 用户信息
        headers.put(HEADER_USER_ID, nullToEmpty(userInfo.getUpdateUserId()));
        headers.put(HEADER_USER_NAME, nullToEmpty(userInfo.getUpdateUserName()));
        
        // 业务维度信息
        headers.put(HEADER_SHOP_ID, nullToEmpty(userInfo.getShopId()));
        headers.put(HEADER_WAREHOUSE_ID, nullToEmpty(userInfo.getWarehouseId()));
        
        // 权限信息
        headers.put(HEADER_ROLE_ID, nullToEmpty(userInfo.getRoleId()));
        headers.put(HEADER_ROLE_NAME, nullToEmpty(userInfo.getRoleName()));
        headers.put(HEADER_PERMISSIONS, nullToEmpty(userInfo.getPermissions()));
    }

    /**
     * 空值处理：null转空字符串
     */
    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}