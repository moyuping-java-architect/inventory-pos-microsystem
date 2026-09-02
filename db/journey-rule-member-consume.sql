-- ============================================================
-- 会员储值消费旅程规则补齐
--
-- 背景：首购 + 四档升级规则原本只挂在 CASHIER.CHECKOUT_COMPLETED 上，
--       而收银订单（order_main / member 表）在 POS 端独立库，
--       主应用 psi-app 并不包含 psi-cashier 模块，该事件永远不会产生。
--       主库真实的会员消费入口是 MemberInfoServiceImpl.consume，
--       它发的是 MEMBER.CARD_CONSUMED，因此把同一批规则复制一份挂上去。
--
-- 指标口径：totalAmount / orderCount 来自 customer_metrics，
--           MEMBER 主体直接读 member_info.total_consume / total_orders，
--           这两个字段在 consume() 里已随消费回写，事件又延迟到事务提交后
--           才投递，所以引擎读到的一定是本单计入后的最新值。
-- ============================================================
SET NAMES utf8mb4;

DELETE FROM touchpoint_generate_rule WHERE event_code = 'MEMBER.CARD_CONSUMED';

INSERT INTO touchpoint_generate_rule
(rule_name, event_code, condition_json, touchpoint_type, intent, channel,
 summary_template, once_only, enabled, sort_order, del_flag, create_time, update_time)
VALUES
('会员首次储值消费', 'MEMBER.CARD_CONSUMED',
 '[{"field":"orderCount","op":"EQ","value":"1"}]',
 'FIRST_PURCHASE', 'BUY', 'SYSTEM',
 '首次消费 {consumeAmount}，余额 {balanceAfter}', 1, 1, 10, 0,
 DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')),

('会员储值消费', 'MEMBER.CARD_CONSUMED',
 NULL,
 'ORDER_COMPLETED', 'BUY', 'SYSTEM',
 '储值消费 {consumeAmount}，累计 {totalAmount}', 0, 1, 11, 0,
 DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')),

('累计满 1000 升铜卡', 'MEMBER.CARD_CONSUMED',
 '[{"field":"totalAmount","op":"GE","value":"1000"}]',
 'MEMBER_UPGRADE', 'BRONZE', 'SYSTEM',
 '累计消费 {totalAmount}，升级铜卡', 1, 1, 20, 0,
 DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')),

('累计满 5000 升银卡', 'MEMBER.CARD_CONSUMED',
 '[{"field":"totalAmount","op":"GE","value":"5000"}]',
 'MEMBER_UPGRADE', 'SILVER', 'SYSTEM',
 '累计消费 {totalAmount}，升级银卡', 1, 1, 21, 0,
 DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')),

('累计满 20000 升金卡', 'MEMBER.CARD_CONSUMED',
 '[{"field":"totalAmount","op":"GE","value":"20000"}]',
 'MEMBER_UPGRADE', 'GOLD', 'SYSTEM',
 '累计消费 {totalAmount}，升级金卡', 1, 1, 22, 0,
 DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')),

('累计满 50000 升黑卡', 'MEMBER.CARD_CONSUMED',
 '[{"field":"totalAmount","op":"GE","value":"50000"}]',
 'MEMBER_UPGRADE', 'BLACK', 'SYSTEM',
 '累计消费 {totalAmount}，升级黑卡', 1, 1, 23, 0,
 DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'));

-- 会员充值也是一次有效触点，老板看得到「这个月谁充了钱」
DELETE FROM touchpoint_generate_rule WHERE event_code = 'MEMBER.RECHARGED';

INSERT INTO touchpoint_generate_rule
(rule_name, event_code, condition_json, touchpoint_type, intent, channel,
 summary_template, once_only, enabled, sort_order, del_flag, create_time, update_time)
VALUES
('会员储值充值', 'MEMBER.RECHARGED',
 NULL,
 'MEMBER_RECHARGE', 'BUY', 'SYSTEM',
 '充值 {rechargeAmount}，余额 {balanceAfter}', 0, 1, 12, 0,
 DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'));

-- 已实现埋点的事件标记为 implemented，配置页可据此提示哪些事件真的会来
UPDATE business_event_dict SET implemented = 1
WHERE event_code IN (
    'SALE.ORDER_APPROVED', 'SALE.ORDER_RETURNED',
    'CUSTOMER.CREATED', 'CUSTOMER.STATUS_CHANGED',
    'MEMBER.REGISTERED', 'MEMBER.RECHARGED', 'MEMBER.CARD_CONSUMED',
    'MEMBER.POINTS_CHANGED', 'MEMBER.LEVEL_UPGRADED',
    'FINANCE.PAYMENT_RECEIVED'
);
