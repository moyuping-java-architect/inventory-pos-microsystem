-- ============================================================
-- 客户生命周期旅程（私域运营核心：生命周期分层 + 流失预警 + 二次关怀）
-- 把 Service 层"实时算流失等级"的能力，沉淀为引擎一等公民旅程，
-- 与 MEMBERSHIP（价值层级）/ SALES_FUNNEL（销售漏斗）并列。
-- 可重复执行：先删后插。
-- ============================================================

-- 1) 字典：新增触点类型与意图（供配置页下拉使用）
INSERT IGNORE INTO journey_dict (tenant_id, dict_type, dict_code, dict_name, subject_type, extra_json, built_in, sort_order, enabled, del_flag, create_time, update_time, create_by, status, update_by)
VALUES
 (NULL,'TOUCHPOINT_TYPE','SILENT_WARNING','沉默预警触点','BOTH',NULL,0,90,1,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'TOUCHPOINT_TYPE','CHURN','流失触点','BOTH',NULL,0,91,1,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'TOUCHPOINT_TYPE','SECOND_CARE','二次关怀触点','BOTH',NULL,0,92,1,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'TOUCHPOINT_TYPE','REACTIVE','唤醒触点','BOTH',NULL,0,93,1,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'INTENT','SILENT','沉默','BOTH',NULL,0,90,1,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'INTENT','CHURNED','流失','BOTH',NULL,0,91,1,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'INTENT','NURTURE','培育','BOTH',NULL,0,92,1,0,NOW(),NOW(),NULL,1,NULL);

-- 2) 事件字典：系统合成的会员生命周期事件（让规则配置页能看到它们）
INSERT IGNORE INTO business_event_dict (event_code, event_name, module_name, subject_type, payload_fields, implemented, sort_order, enabled, create_time, update_time, create_by, update_by, status)
VALUES
 ('JOURNEY.MEMBER_SILENT','会员沉默预警(系统合成)','customer','MEMBER','daysSinceLast',1,90,1,NOW(),NOW(),NULL,NULL,1),
 ('JOURNEY.MEMBER_CHURNED','会员流失判定(系统合成)','customer','MEMBER','daysSinceLast',1,91,1,NOW(),NOW(),NULL,NULL,1);

-- 3) 旅程模板 + 阶段
DELETE FROM customer_journey_stage WHERE journey_code='LIFECYCLE';
DELETE FROM customer_journey_template WHERE journey_code='LIFECYCLE';

INSERT INTO customer_journey_template (tenant_id, journey_code, journey_name, subject_type, icon, description, is_default, enabled, sort_order, del_flag, create_time, update_time, create_by, status, update_by)
VALUES (NULL, 'LIFECYCLE', '客户生命周期', 'MEMBER', 'Lifecycle', '注册→活跃→沉默→流失→唤醒：基于距上次消费天数的动态分层（私域运营核心）', 0, 1, 200, 0, NOW(), NOW(), NULL, 1, NULL);

INSERT INTO customer_journey_stage (tenant_id, journey_code, stage_code, stage_name, color, icon, tip, match_touchpoint, match_intent, sort_order, allow_rollback, del_flag, create_time, update_time, create_by, status, update_by)
VALUES
 (NULL,'LIFECYCLE','LC_NEW','新客','#909399','User','刚注册或首单内的新客','MEMBER_REGISTERED',NULL,10,0,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'LIFECYCLE','LC_ACTIVE','活跃','#67C23A','User','近期有消费，处于正常复购周期','ORDER_COMPLETED','BUY',20,1,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'LIFECYCLE','LC_SILENT','沉默预警','#E6A23C','Warning','超过正常复购周期未回店','SILENT_WARNING','SILENT',30,0,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'LIFECYCLE','LC_CHURNED','已流失','#F56C6C','Delete','远超复购周期未回店，判定流失','CHURN','CHURNED',40,0,0,NOW(),NOW(),NULL,1,NULL);

-- 4) 规则
DELETE FROM touchpoint_generate_rule WHERE event_code IN ('JOURNEY.MEMBER_SILENT','JOURNEY.MEMBER_CHURNED') OR (event_code='MEMBER.CARD_CONSUMED' AND touchpoint_type='SECOND_CARE');

-- 系统合成事件 → 生成对应触点（条件恒真，无 condition_json）
INSERT INTO touchpoint_generate_rule (tenant_id, rule_name, event_code, condition_json, touchpoint_type, intent, channel, summary_template, once_only, enabled, sort_order, del_flag, create_time, update_time, create_by, status, update_by)
VALUES
 (NULL,'会员沉默预警','JOURNEY.MEMBER_SILENT',NULL,'SILENT_WARNING','SILENT','SYSTEM','距上次消费{daysSinceLast}天，进入沉默预警',0,1,10,0,NOW(),NOW(),NULL,1,NULL),
 (NULL,'会员流失判定','JOURNEY.MEMBER_CHURNED',NULL,'CHURN','CHURNED','SYSTEM','距上次消费{daysSinceLast}天，判定流失',0,1,20,0,NOW(),NOW(),NULL,1,NULL),
-- 二次关怀（私域运营关键节点：首购→二次销售黄金期）：首购那单 orderCount==1 时触发
 (NULL,'新客首购二次关怀','MEMBER.CARD_CONSUMED','[{"field":"orderCount","op":"EQ","value":"1"}]','SECOND_CARE','NURTURE','SYSTEM','新客首购完成，3天内做二次关怀（发券/回访）',1,1,13,0,NOW(),NOW(),NULL,1,NULL);

SELECT 'LIFECYCLE journey seeded' AS result;
