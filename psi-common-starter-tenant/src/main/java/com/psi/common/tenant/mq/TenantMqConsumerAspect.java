package com.psi.common.tenant.mq;

import com.psi.common.context.UserContext;
import com.psi.common.context.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MQ消费者租户上下文切面
 * 
 * <p>自动拦截所有 @RabbitListener 注解的方法，从消息头提取租户信息设置到上下文，
 * 消费完成后自动清理上下文，确保租户信息在MQ消费链路中正确透传。
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class TenantMqConsumerAspect {

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

    /**
     * 切入点：所有 @RabbitListener 注解的方法
     */
    @Pointcut("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public void rabbitListenerMethods() {
    }

    /**
     * 环绕通知：设置租户上下文 -> 执行消费 -> 清理上下文
     */
    @Around("rabbitListenerMethods()")
    public Object interceptConsumer(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 从消息参数中提取租户信息并设置上下文
            extractAndSetTenantContext(joinPoint);
            
            // 执行原始的消费方法
            return joinPoint.proceed();
            
        } finally {
            // 无论成功还是失败，都清理上下文
            clearTenantContext();
        }
    }

    /**
     * 从方法参数中提取消息，并设置租户上下文
     */
    private void extractAndSetTenantContext(ProceedingJoinPoint joinPoint) {
        Message message = extractMessageFromArgs(joinPoint.getArgs());
        if (message == null) {
            log.debug("No Message found in consumer method arguments");
            return;
        }

        try {
            UserInfo userInfo = extractUserInfoFromMessage(message);
            if (userInfo != null) {
                UserContext.set(userInfo);
                log.debug("Tenant context set from MQ message: tenantId={}, userId={}", 
                        userInfo.getTenantId(), userInfo.getUpdateUserId());
            }
        } catch (Exception e) {
            log.warn("Failed to extract tenant context from MQ message", e);
        }
    }

    /**
     * 从方法参数中提取 Message 对象
     */
    private Message extractMessageFromArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        for (Object arg : args) {
            if (arg instanceof Message) {
                return (Message) arg;
            }
        }
        return null;
    }

    /**
     * 从消息头中提取用户信息
     */
    private UserInfo extractUserInfoFromMessage(Message message) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        
        UserInfo userInfo = new UserInfo();
        userInfo.setTenantId(getHeaderValue(headers, HEADER_TENANT_ID));
        userInfo.setUpdateUserId(getHeaderValue(headers, HEADER_USER_ID));
        userInfo.setUpdateUserName(getHeaderValue(headers, HEADER_USER_NAME));
        userInfo.setShopId(getHeaderValue(headers, HEADER_SHOP_ID));
        userInfo.setWarehouseId(getHeaderValue(headers, HEADER_WAREHOUSE_ID));
        userInfo.setRoleId(getHeaderValue(headers, HEADER_ROLE_ID));
        userInfo.setRoleName(getHeaderValue(headers, HEADER_ROLE_NAME));
        userInfo.setPermissions(getHeaderValue(headers, HEADER_PERMISSIONS));
        
        return userInfo;
    }

    /**
     * 获取消息头值
     */
    private String getHeaderValue(Map<String, Object> headers, String key) {
        Object value = headers.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * 清理租户上下文
     */
    private void clearTenantContext() {
        try {
            UserContext.clearAll();
            log.debug("Tenant context cleared after MQ message consumption");
        } catch (Exception e) {
            log.warn("Failed to clear tenant context", e);
        }
    }
}