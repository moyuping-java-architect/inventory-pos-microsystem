package com.psi.common.constant;

/**
 * RabbitMQ 常量类
 * 定义公共交换机、队列、路由键及死信相关常量
 * 
 * @author PSI
 * @version 1.0.0
 */
public class RabbitMQConstant {

    // ==================== 公共配置 ====================
    
    /**
     * 公共交换机
     */
    public static final String COMMON_EXCHANGE = "common.exchange";

    /**
     * 公共队列
     */
    public static final String COMMON_QUEUE = "common.queue";

    /**
     * 公共路由键
     */
    public static final String COMMON_ROUTING_KEY = "common.routing";

    // ==================== 死信配置 ====================

    /**
     * 公共死信交换机
     */
    public static final String COMMON_DLX_EXCHANGE = "common.dlx.exchange";

    /**
     * 公共死信队列
     */
    public static final String COMMON_DLX_QUEUE = "common.dlx.queue";

    /**
     * 公共死信路由键
     */
    public static final String COMMON_DLX_ROUTING_KEY = "common.dlx.routing";

    /**
     * 死信路由键前缀（按业务拆分）
     */
    public static final String DLX_ROUTING_KEY_PREFIX = "dlx.";

    /**
     * 采购流程死信路由键
     */
    public static final String DLX_PURCHASE_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "purchase";

    /**
     * 销售流程死信路由键
     */
    public static final String DLX_SALE_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "sale";

    /**
     * 库存流程死信路由键
     */
    public static final String DLX_STOCK_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "stock";

    /**
     * 商品流程死信路由键
     */
    public static final String DLX_GOODS_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "goods";

    /**
     * 通用流程死信路由键
     */
    public static final String DLX_COMMON_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "common";

    /**
     * 消息记录死信路由键
     */
    public static final String DLX_MESSAGE_RECORD_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "message.record";

    /**
     * 登录日志死信路由键
     */
    public static final String DLX_LOGIN_LOG_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "login.log";

    /**
     * 财务死信路由键
     */
    public static final String DLX_FINANCE_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "finance";

    /**
     * 同步死信路由键
     */
    public static final String DLX_SYNC_ROUTING_KEY = DLX_ROUTING_KEY_PREFIX + "sync";

    /**
     * 采购流程死信队列
     */
    public static final String DLX_PURCHASE_QUEUE = "common.dlx.purchase.queue";

    /**
     * 销售流程死信队列
     */
    public static final String DLX_SALE_QUEUE = "common.dlx.sale.queue";

    /**
     * 库存流程死信队列
     */
    public static final String DLX_STOCK_QUEUE = "common.dlx.stock.queue";

    /**
     * 商品流程死信队列
     */
    public static final String DLX_GOODS_QUEUE = "common.dlx.goods.queue";

    /**
     * 通用流程死信队列
     */
    public static final String DLX_COMMON_QUEUE = "common.dlx.common.queue";

    /**
     * 消息记录死信队列
     */
    public static final String DLX_MESSAGE_RECORD_QUEUE = "common.dlx.message.record.queue";

    /**
     * 登录日志死信队列
     */
    public static final String DLX_LOGIN_LOG_QUEUE = "common.dlx.login.log.queue";

    /**
     * 财务死信队列
     */
    public static final String DLX_FINANCE_QUEUE = "common.dlx.finance.queue";

    /**
     * 同步死信队列
     */
    public static final String DLX_SYNC_QUEUE = "common.dlx.sync.queue";

    // ==================== 消息头常量 ====================

    /**
     * 延迟消息头（用于 RabbitMQ 延迟插件）
     */
    public static final String HEADER_DELAY = "x-delay";

    /**
     * 死信交换机消息头
     */
    public static final String HEADER_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    /**
     * 死信路由键消息头
     */
    public static final String HEADER_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    // ==================== 消息持久化配置====================
    public static final String MESSAGE_RECORD_EXCHANGE = "psi.message.record";
    public static final String MESSAGE_RECORD_QUEUE = "psi.message.record.queue";
    public static final String MESSAGE_RECORD_ROUTING_KEY = "message.record";

    // ==================== 登录日志配置====================
    /**
     * 登录日志交换机
     */
    public static final String LOGIN_LOG_EXCHANGE = "system.login.log.exchange";

    /**
     * 登录日志队列
     */
    public static final String LOGIN_LOG_QUEUE = "system.login.log.queue";

    /**
     * 登录日志路由键
     */
    public static final String LOGIN_LOG_ROUTING_KEY = "login.log";

    // ==================== 流程完成通知配置 ====================
    
    /**
     * 流程完成通知交换机（订单业务）
     */
    public static final String PROCESS_COMPLETED_ORDER_EXCHANGE = "flow.completed.order.exchange";
    
    /**
     * 流程完成通知队列（订单业务）
     */
    public static final String PROCESS_COMPLETED_ORDER_QUEUE = "flow.completed.order.queue";
    
    /**
     * 流程完成通知路由键（订单业务）
     */
    public static final String PROCESS_COMPLETED_ORDER_ROUTING_KEY = "flow.completed.order";

    /**
     * 流程完成通知交换机（商品业务）
     */
    public static final String PROCESS_COMPLETED_GOODS_EXCHANGE = "flow.completed.goods.exchange";
    
    /**
     * 流程完成通知队列（商品业务）
     */
    public static final String PROCESS_COMPLETED_GOODS_QUEUE = "flow.completed.goods.queue";
    
    /**
     * 流程完成通知路由键（商品业务）
     */
    public static final String PROCESS_COMPLETED_GOODS_ROUTING_KEY = "flow.completed.goods";

    /**
     * 流程完成通知交换机（库存业务）
     */
    public static final String PROCESS_COMPLETED_STOCK_EXCHANGE = "flow.completed.stock.exchange";
    
    /**
     * 流程完成通知队列（库存业务）
     */
    public static final String PROCESS_COMPLETED_STOCK_QUEUE = "flow.completed.stock.queue";
    
    /**
     * 流程完成通知路由键（库存业务）
     */
    public static final String PROCESS_COMPLETED_STOCK_ROUTING_KEY = "flow.completed.stock";

    /**
     * 流程完成通知交换机（通用业务）
     */
    public static final String PROCESS_COMPLETED_COMMON_EXCHANGE = "flow.completed.common.exchange";
    
    /**
     * 流程完成通知队列（通用业务）
     */
    public static final String PROCESS_COMPLETED_COMMON_QUEUE = "flow.completed.common.queue";
    
    /**
     * 流程完成通知路由键（通用业务）
     */
    public static final String PROCESS_COMPLETED_COMMON_ROUTING_KEY = "flow.completed.common";

    // ==================== 单据业务流程完成通知配置 ====================
    
    /**
     * 流程完成通知交换机（采购业务）
     */
    public static final String PROCESS_COMPLETED_PURCHASE_EXCHANGE = "flow.completed.purchase.exchange";
    
    /**
     * 流程完成通知队列（采购业务）
     */
    public static final String PROCESS_COMPLETED_PURCHASE_QUEUE = "flow.completed.purchase.queue";
    
    /**
     * 流程完成通知路由键（采购业务）
     */
    public static final String PROCESS_COMPLETED_PURCHASE_ROUTING_KEY = "flow.completed.purchase";

    /**
     * 流程完成通知交换机（销售业务）
     */
    public static final String PROCESS_COMPLETED_SALE_EXCHANGE = "flow.completed.sale.exchange";
    
    /**
     * 流程完成通知队列（销售业务）
     */
    public static final String PROCESS_COMPLETED_SALE_QUEUE = "flow.completed.sale.queue";
    
    /**
     * 流程完成通知路由键（销售业务）
     */
    public static final String PROCESS_COMPLETED_SALE_ROUTING_KEY = "flow.completed.sale";

    /**
     * 流程完成通知交换机（报损业务）
     */
    public static final String PROCESS_COMPLETED_LOSS_EXCHANGE = "flow.completed.loss.exchange";
    
    /**
     * 流程完成通知队列（报损业务）
     */
    public static final String PROCESS_COMPLETED_LOSS_QUEUE = "flow.completed.loss.queue";
    
    /**
     * 流程完成通知路由键（报损业务）
     */
    public static final String PROCESS_COMPLETED_LOSS_ROUTING_KEY = "flow.completed.loss";

    /**
     * 流程完成通知交换机（报溢业务）
     */
    public static final String PROCESS_COMPLETED_OVERFLOW_EXCHANGE = "flow.completed.overflow.exchange";
    
    /**
     * 流程完成通知队列（报溢业务）
     */
    public static final String PROCESS_COMPLETED_OVERFLOW_QUEUE = "flow.completed.overflow.queue";
    
    /**
     * 流程完成通知路由键（报溢业务）
     */
    public static final String PROCESS_COMPLETED_OVERFLOW_ROUTING_KEY = "flow.completed.overflow";

    /**
     * 流程完成通知交换机（盘点业务）
     */
    public static final String PROCESS_COMPLETED_CHECK_EXCHANGE = "flow.completed.check.exchange";
    
    /**
     * 流程完成通知队列（盘点业务）
     */
    public static final String PROCESS_COMPLETED_CHECK_QUEUE = "flow.completed.check.queue";
    
    /**
     * 流程完成通知路由键（盘点业务）
     */
    public static final String PROCESS_COMPLETED_CHECK_ROUTING_KEY = "flow.completed.check";

    // ==================== 销售业务MQ配置 ====================

    // ==================== 收银业务 ====================

    /**
     * 收银业务交换机
     */
    public static final String CASHIER_EXCHANGE = "psi.sale.cashier.exchange";

    /**
     * 收银财务路由键
     */
    public static final String CASHIER_FINANCE_ROUTING_KEY = "cashier.finance";

    /**
     * 收银库存路由键
     */
    public static final String CASHIER_STOCK_ROUTING_KEY = "cashier.stock";

    /**
     * 队列 - 收银财务
     */
    public static final String CASHIER_FINANCE_QUEUE = "psi.sale.cashier.finance.queue";

    /**
     * 队列 - 收银库存
     */
    public static final String CASHIER_STOCK_QUEUE = "psi.sale.cashier.stock.queue";

    // ==================== 销售订单业务 ====================

    /**
     * 销售订单交换机
     */
    public static final String SALE_ORDER_EXCHANGE = "psi.sale.order.exchange";

    /**
     * 销售订单财务路由键
     */
    public static final String SALE_ORDER_FINANCE_ROUTING_KEY = "sale.order.finance";

    /**
     * 销售订单库存路由键
     */
    public static final String SALE_ORDER_STOCK_ROUTING_KEY = "sale.order.stock";

    /**
     * 销售订单释放库存路由键（订单取消/驳回时释放预占库存）
     */
    public static final String SALE_ORDER_RELEASE_ROUTING_KEY = "sale.order.release";

    /**
     * 队列 - 销售订单财务
     */
    public static final String SALE_ORDER_FINANCE_QUEUE = "psi.sale.order.finance.queue";

    /**
     * 队列 - 销售订单库存
     */
    public static final String SALE_ORDER_STOCK_QUEUE = "psi.sale.order.stock.queue";

    /**
     * 队列 - 销售订单释放库存
     */
    public static final String SALE_ORDER_RELEASE_QUEUE = "psi.sale.order.release.queue";

    // ==================== 销售出库业务 ====================

    /**
     * 销售出库交换机
     */
    public static final String SALE_OUT_EXCHANGE = "psi.sale.out.exchange";

    /**
     * 销售出库财务路由键
     */
    public static final String SALE_OUT_FINANCE_ROUTING_KEY = "sale.out.finance";

    /**
     * 销售出库库存路由键
     */
    public static final String SALE_OUT_STOCK_ROUTING_KEY = "sale.out.stock";

    /**
     * 队列 - 销售出库财务
     */
    public static final String SALE_OUT_FINANCE_QUEUE = "psi.sale.out.finance.queue";

    /**
     * 队列 - 销售出库库存
     */
    public static final String SALE_OUT_STOCK_QUEUE = "psi.sale.out.stock.queue";

    // ==================== 销售退货业务 ====================

    /**
     * 销售退货交换机
     */
    public static final String SALE_RETURN_EXCHANGE = "psi.sale.return.exchange";

    /**
     * 销售退货财务路由键
     */
    public static final String SALE_RETURN_FINANCE_ROUTING_KEY = "sale.return.finance";

    /**
     * 销售退货库存路由键
     */
    public static final String SALE_RETURN_STOCK_ROUTING_KEY = "sale.return.stock";

    /**
     * 队列 - 销售退货财务
     */
    public static final String SALE_RETURN_FINANCE_QUEUE = "psi.sale.return.finance.queue";

    /**
     * 队列 - 销售退货库存
     */
    public static final String SALE_RETURN_STOCK_QUEUE = "psi.sale.return.stock.queue";

    // ==================== 采购入库业务 ====================

    /**
     * 采购入库交换机
     */
    public static final String PURCHASE_IN_EXCHANGE = "psi.purchase.in.exchange";

    /**
     * 采购入库库存路由键
     */
    public static final String PURCHASE_IN_STOCK_ROUTING_KEY = "purchase.in.stock";

    /**
     * 队列 - 采购入库库存
     */
    public static final String PURCHASE_IN_STOCK_QUEUE = "psi.purchase.in.stock.queue";

    // ==================== 采购退货业务 ====================

    /**
     * 采购退货交换机
     */
    public static final String PURCHASE_RETURN_EXCHANGE = "psi.purchase.return.exchange";

    /**
     * 采购退货库存路由键
     */
    public static final String PURCHASE_RETURN_STOCK_ROUTING_KEY = "purchase.return.stock";

    /**
     * 队列 - 采购退货库存
     */
    public static final String PURCHASE_RETURN_STOCK_QUEUE = "psi.purchase.return.stock.queue";

    // ==================== 数据同步MQ配置 ====================

    /**
     * 上行同步交换机（POS → sync-ms）
     */
    public static final String SYNC_UP_EXCHANGE = "psi.sync.up.exchange";

    /**
     * 上行同步队列（POS → sync-ms）
     */
    public static final String SYNC_UP_QUEUE = "psi.sync.up.queue";

    /**
     * 上行同步队列（商品数据）
     */
    public static final String SYNC_UP_GOODS_QUEUE = "psi.goods.sync.up.queue";

    /**
     * 上行同步路由键
     */
    public static final String SYNC_UP_ROUTING_KEY = "sync.up";

    /**
     * 上行同步路由键（订单数据）
     */
    public static final String SYNC_UP_ORDER_ROUTING_KEY = "sync.up.order";

    /**
     * 上行同步路由键（会员数据）
     */
    public static final String SYNC_UP_MEMBER_ROUTING_KEY = "sync.up.member";

    /**
     * 上行同步路由键（退款数据）
     */
    public static final String SYNC_UP_REFUND_ROUTING_KEY = "sync.up.refund";

    /**
     * 上行同步路由键（日结数据）
     */
    public static final String SYNC_UP_SETTLEMENT_ROUTING_KEY = "sync.up.settlement";

    /**
     * 上行同步路由键（班次数据）
     */
    public static final String SYNC_UP_SHIFT_ROUTING_KEY = "sync.up.shift";

    /**
     * 上行同步路由键（商品数据）
     */
    public static final String SYNC_UP_GOODS_ROUTING_KEY = "sync.up.goods";

    /**
     * 上行同步客户队列
     */
    public static final String SYNC_UP_CUSTOMER_QUEUE = "psi.sync.up.customer.queue";

    /**
     * 上行同步客户路由键
     */
    public static final String SYNC_UP_CUSTOMER_ROUTING_KEY = "sync.up.customer";

    /**
     * 上行同步采购队列
     */
    public static final String SYNC_UP_PURCHASE_QUEUE = "psi.sync.up.purchase.queue";

    /**
     * 上行同步采购路由键
     */
    public static final String SYNC_UP_PURCHASE_ROUTING_KEY = "sync.up.purchase";

    /**
     * 上行同步库存队列
     */
    public static final String SYNC_UP_STOCK_QUEUE = "psi.sync.up.stock.queue";

    /**
     * 上行同步库存路由键
     */
    public static final String SYNC_UP_STOCK_ROUTING_KEY = "sync.up.stock";

    /**
     * 上行同步财务队列
     */
    public static final String SYNC_UP_FINANCE_QUEUE = "psi.sync.up.finance.queue";

    /**
     * 上行同步财务路由键
     */
    public static final String SYNC_UP_FINANCE_ROUTING_KEY = "sync.up.finance";

    // ==================== 下行同步MQ配置（后台 → POS） ====================

    /**
     * 下行同步交换机（后台 → POS）
     */
    public static final String SYNC_DOWN_EXCHANGE = "psi.sync.down.exchange";

    /**
     * 下行同步队列（后台 → POS）
     */
    public static final String SYNC_DOWN_QUEUE = "psi.sync.down.queue";

    /**
     * 下行同步路由键
     */
    public static final String SYNC_DOWN_ROUTING_KEY = "sync.down";

    /**
     * 下行同步路由键（收银机配置）
     */
    public static final String SYNC_DOWN_POS_CONFIG_ROUTING_KEY = "sync.down.pos.config";

    /**
     * 下行同步路由键（收银员）
     */
    public static final String SYNC_DOWN_POS_OPERATOR_ROUTING_KEY = "sync.down.pos.operator";

}