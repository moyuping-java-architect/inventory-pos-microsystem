package com.psi.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 用户上下文
 * 用于在请求中记录用户信息
 * 主要提供3个方法get,set,clearAll
 * 
 * @author PSI
 * @version 2.0.0
 */
public class UserContext {
    private static final TransmittableThreadLocal<UserInfo> HOLDER = new TransmittableThreadLocal<>();
    
    /**
     * 追踪栈上下文 - 存储调用链节点
     * 使用 Deque 实现栈结构，支持线程间透传
     */
    private static final TransmittableThreadLocal<String> TRACE_ID =
            new TransmittableThreadLocal<>();

    /**
     * 获取用户上下文
     * @return 用户信息
     */
    public static UserInfo get() {
        return HOLDER.get();
    }

    /**
     * 设置用户上下文
     * @param userInfo 用户信息
     */
    public static void set(UserInfo userInfo) {
        HOLDER.set(userInfo);
    }
    /**
     * 获取用户上下文
     * @return 用户信息
     */
    public static String getTraceId() {
        return TRACE_ID.get();
    }

    /**
     * 设置用户上下文
     * @param traceId 追踪ID
     *
     */
    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    /**
     * 获取租户ID
     * @return 租户ID
     */
    public static String getTenantId() {
        UserInfo userInfo = HOLDER.get();
        return userInfo != null ? userInfo.getTenantId() : null;
    }

    /**
     * 设置租户ID
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        UserInfo userInfo = HOLDER.get();
        if (userInfo == null) {
            userInfo = new UserInfo();
            HOLDER.set(userInfo);
        }
        userInfo.setTenantId(tenantId);
    }

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public static String getUserId() {
        UserInfo userInfo = HOLDER.get();
        return userInfo != null ? userInfo.getUpdateUserId() : null;
    }

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        UserInfo userInfo = HOLDER.get();
        if (userInfo == null) {
            userInfo = new UserInfo();
            HOLDER.set(userInfo);
        }
        userInfo.setUpdateUserId(userId);
    }

    /**
     * 清除用户上下文
     */
    public static void clearAll() {
        HOLDER.remove();
        TRACE_ID.remove();
    }
}