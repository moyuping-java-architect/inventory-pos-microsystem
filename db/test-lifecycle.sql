-- 造 3 个"时间错位"的会员用于验证 LIFECYCLE 旅程
-- T1: 老客户(order_count=8) 但 120 天没消费 -> CHURNED
-- T2: 新客(order_count=1) 120 天没回 -> NEW_CHURNED -> CHURNED
-- T3: 活跃客户(order_count=5) 45 天没消费(无频率数据走固定阈值) -> AT_RISK -> SILENT
DELETE FROM member_info WHERE member_no IN ('LCTEST1','LCTEST2','LCTEST3');

INSERT INTO member_info (member_no, member_name, phone, gender, register_time, last_consume_time, total_orders, total_consume, balance, points, level_name, del_flag, tenant_id, status)
VALUES
 ('LCTEST1','生命周期测试-流失老客','0971000001',0, DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_SUB(NOW(), INTERVAL 120 DAY), 8, 8000.00, 0, 0, '普通会员', 0, 1, 1),
 ('LCTEST2','生命周期测试-新客流失','0971000002',0, DATE_SUB(NOW(), INTERVAL 130 DAY), DATE_SUB(NOW(), INTERVAL 120 DAY), 1, 500.00, 0, 0, '普通会员', 0, 1, 1),
 ('LCTEST3','生命周期测试-沉默预警','0971000003',0, DATE_SUB(NOW(), INTERVAL 100 DAY), DATE_SUB(NOW(), INTERVAL 45 DAY), 5, 3000.00, 0, 0, '普通会员', 0, 1, 1);

SELECT id, member_no, total_orders, last_consume_time FROM member_info WHERE member_no IN ('LCTEST1','LCTEST2','LCTEST3');
