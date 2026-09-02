-- ============================================================
-- PSI Customer Growth Module - 客户旅程触点表
-- 记录非购买类触点（WhatsApp问价/到店/电话/社交互动等）
-- 购买类触点（下单/付款/退款）自动从 order_main / order_pay 聚合，不入此表
-- ============================================================

CREATE TABLE IF NOT EXISTS `customer_touchpoint` (
    `id`              INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `tenant_id`       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    `member_id`       BIGINT        DEFAULT NULL COMMENT '会员ID（关联 member_info.id）',
    `customer_id`     BIGINT        DEFAULT NULL COMMENT '客户ID（关联 customer.id，B2B大客户用）',
    `touchpoint_type` VARCHAR(32)   NOT NULL COMMENT '触点类型：INQUIRY-问价 / NEGOTIATION-砍价 / WHATSAPP_MSG-WA消息 / STORE_VISIT-到店 / PHONE_CALL-电话 / SOCIAL-社交 / OTHER-其他',
    `channel`         VARCHAR(32)   DEFAULT NULL COMMENT '渠道：WHATSAPP / PHONE / IN_STORE / FACEBOOK / OTHER',
    `contact_time`    VARCHAR(32)   NOT NULL COMMENT '接触时间 yyyy-MM-dd HH:mm:ss',
    `summary`         VARCHAR(500)  DEFAULT NULL COMMENT '触点摘要（老板/店员手写一句话）',
    `intent`          VARCHAR(32)   DEFAULT NULL COMMENT '客户意图：BUY-想买 / COMPARE-比价 / COMPLAINT-投诉 / INFO-咨询 / CHITCHAT-闲聊',
    `follow_up`       VARCHAR(500)  DEFAULT NULL COMMENT '跟进动作（给老板的提醒）',
    `follow_up_done`  TINYINT       DEFAULT 0 COMMENT '跟进是否完成 0-未完成 1-已完成',
    `operator`        VARCHAR(64)   DEFAULT NULL COMMENT '记录人',
    `create_time`     VARCHAR(32)   DEFAULT NULL COMMENT '创建时间',
    `update_time`     VARCHAR(32)   DEFAULT NULL COMMENT '更新时间',
    `del_flag`        TINYINT       DEFAULT 0 COMMENT '删除标志 0-正常 1-已删除',

    INDEX `idx_member_touchpoint` (`member_id`, `touchpoint_type`),
    INDEX `idx_customer_touchpoint` (`customer_id`, `touchpoint_type`),
    INDEX `idx_contact_time` (`contact_time`),
    INDEX `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户旅程触点记录（非购买类）';
