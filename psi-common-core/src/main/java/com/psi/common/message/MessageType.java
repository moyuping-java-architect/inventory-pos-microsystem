package com.psi.common.message;

/**
 * 消息类型枚举
 */
public enum MessageType {
    
    // ==================== 系统消息 ====================
    SYSTEM_LOGIN("SYSTEM_LOGIN", "登录日志"),
    SYSTEM_LOGOUT("SYSTEM_LOGOUT", "登出日志"),
    SYSTEM_OPERATION("SYSTEM_OPERATION", "操作日志"),
    
    // ==================== 订单消息 ====================
    ORDER_CREATED("ORDER_CREATED", "订单创建"),
    ORDER_PAID("ORDER_PAID", "订单支付"),
    ORDER_CANCELLED("ORDER_CANCELLED", "订单取消"),
    ORDER_SHIPPED("ORDER_SHIPPED", "订单发货"),
    ORDER_COMPLETED("ORDER_COMPLETED", "订单完成"),
    
    // ==================== 商品消息 ====================
    GOODS_ADDED("GOODS_ADDED", "商品添加"),
    GOODS_UPDATED("GOODS_UPDATED", "商品更新"),
    GOODS_DELETED("GOODS_DELETED", "商品删除"),
    GOODS_STOCK_CHANGED("GOODS_STOCK_CHANGED", "库存变更"),
    
    // ==================== 库存消息 ====================
    STOCK_IN("STOCK_IN", "入库"),
    STOCK_OUT("STOCK_OUT", "出库"),
    STOCK_TRANSFER("STOCK_TRANSFER", "库存转移"),
    
    // ==================== 消息服务消息 ====================
    MESSAGE_SENT("MESSAGE_SENT", "消息发送"),
    MESSAGE_RECEIVED("MESSAGE_RECEIVED", "消息接收"),
    MESSAGE_DEAD_LETTER("MESSAGE_DEAD_LETTER", "死信消息"),
    
    // ==================== 流程消息 ====================
    FLOW_STARTED("FLOW_STARTED", "流程启动"),
    FLOW_APPROVED("FLOW_APPROVED", "流程审批通过"),
    FLOW_REJECTED("FLOW_REJECTED", "流程拒绝"),
    FLOW_COMPLETED("FLOW_COMPLETED", "流程完成"),
    
    // ==================== 通用消息 ====================
    GENERAL_NOTIFY("GENERAL_NOTIFY", "通用通知"),
    GENERAL_ALERT("GENERAL_ALERT", "通用告警"),
    GENERAL_SYNC("GENERAL_SYNC", "数据同步");
    
    private final String code;
    private final String description;
    
    MessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return GENERAL_NOTIFY;
    }
}