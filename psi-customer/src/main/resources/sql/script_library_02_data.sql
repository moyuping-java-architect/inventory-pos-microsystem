-- 话术库 * 销冠 默认数据（旅程触达计划版）
-- 阶段编码对齐 SALES_FUNNEL 旅程：STAGE_INTENTION / STAGE_DEMO / STAGE_PROPOSAL / STAGE_PRICE / STAGE_SIGNED
-- 示例：同一节点第1/7/15/30天分别发送不同话术，且去重

SET NAMES utf8mb4;

-- 初始化示例标签（与种子客户标签一致）
INSERT IGNORE INTO customer_tag (tenant_id, tag_code, tag_name, category, color, sort, create_time, update_time, del_flag) VALUES
('1', 'NEW', '新客户', 'LIFECYCLE', '#67C23A', 10, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),
('1', 'HIGH_VALUE', '高价值客户', 'VALUE', '#409EFF', 20, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),
('1', 'PRICE_SENSITIVE', '价格敏感', 'BEHAVIOR', '#E6A23C', 30, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),
('1', 'WHATSAPP_USER', 'WhatsApp用户', 'CHANNEL', '#F56C6C', 40, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),
('1', 'SMS_USER', '短信用户', 'CHANNEL', '#909399', 50, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0);

-- 初始化示例话术：按 SALES_FUNNEL 阶段 + 停留天数递增触达
INSERT IGNORE INTO sales_script_library
(tenant_id, script_code, script_name, category, content, channel, stage_code, stage_name, tags, tag_match_mode, day_start, day_end, priority, send_limit, is_enabled, create_time, update_time, del_flag)
VALUES
-- ===== STAGE_INTENTION 意向客户 =====
-- 第1天：破冰+发资料（新客户/WhatsApp用户 均可）
('1', 'INTENTION_D1_V1', '意向第1天：破冰+发案例', 'ICE_BREAK',
 'Hi {{name}}，很高兴认识你！我先发一份同类型客户的落地案例给你，方便你内部评估时参考。有任何问题随时在 WhatsApp 找我～',
 'WHATSAPP', 'STAGE_INTENTION', '意向客户', 'NEW', 'ANY', 1, 999, 10, 1, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),

-- 第7天：方案对比
('1', 'INTENTION_D7_V1', '意向第7天：方案对比', 'SOLUTION',
 '{{name}}，过去一周我们聊的方案，我整理了一份 A/B 对比：A 重前期投入、B 重按月付费。如果这季度预算紧，B 可以先试点跑通再扩。要我发份样例数据吗？',
 'WHATSAPP', 'STAGE_INTENTION', '意向客户', 'WHATSAPP_USER', 'ANY', 7, 14, 20, 1, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),

-- 第15天：缓解预算顾虑
('1', 'INTENTION_D15_V1', '意向第15天：缓解预算顾虑', 'SOLUTION',
 '{{name}}，半个月了，如果一次性采购预算批不下来，我们可以先从一个校区按月付费试点，跑通后再推广到其他校区。',
 'WHATSAPP', 'STAGE_INTENTION', '意向客户', 'PRICE_SENSITIVE', 'ANY', 15, 29, 30, 1, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),

-- 第30天：逼单/窗口期
('1', 'INTENTION_D30_V1', '意向第30天：季度窗口逼单', 'CLOSE',
 '{{name}}，方案已经确认三遍了，这个月是这季度采购窗口的最后一周，如果今天能定下来，我帮你申请把实施周期从 2 周压缩到 1 周。',
 'WHATSAPP', 'STAGE_INTENTION', '意向客户', 'HIGH_VALUE', 'ANY', 30, 999, 40, 1, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),

-- ===== STAGE_DEMO 拜访演示 =====
('1', 'DEMO_D1_V1', '演示第1天：确认需求', 'SOLUTION',
 '{{name}}，昨天的演示辛苦了！我整理了一份演示中提到的功能清单和对应你们场景的价值点，方便你内部同步。',
 'WHATSAPP', 'STAGE_DEMO', '拜访演示', 'WHATSAPP_USER', 'ANY', 1, 999, 10, 1, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),

-- ===== STAGE_PROPOSAL 方案报价 =====
('1', 'PROPOSAL_D1_V1', '报价第1天：方案确认', 'CLOSE',
 '{{name}}，报价方案已发出，麻烦确认下范围是否覆盖你们这学期的目标校区。如果有调整我当天就能更新。',
 'WHATSAPP', 'STAGE_PROPOSAL', '方案报价', 'HIGH_VALUE', 'ANY', 1, 999, 10, 1, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),

-- ===== STAGE_PRICE 价格谈判 =====
('1', 'PRICE_D1_V1', '谈判第1天：确认决策者', 'CLOSE',
 '{{name}}，方案基本定了，麻烦确认一下内部决策流程和预计签约时间，我好提前预留实施资源。',
 'SMS', 'STAGE_PRICE', '价格谈判', 'PRICE_SENSITIVE', 'ANY', 1, 999, 10, 1, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0),

-- ===== STAGE_SIGNED 已签约 =====
('1', 'SIGNED_D30_V1', '签约第30天：复购唤醒', 'REACTIVATE',
 '{{name}}，你们签约满一个月啦！使用过程中有没有需要优化的点？这学期新增校区的话有打包价，要不要我发你看看？',
 'WHATSAPP', 'STAGE_SIGNED', '已签约', 'HIGH_VALUE', 'ANY', 30, 999, 10, 1, 1, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 0);
