-- 为客户看板 6 个客户补旅程状态 + 标签，便于演示阶段触达
SET NAMES utf8mb4;

-- 清空旧 CUSTOMER 类型状态（如有），重新造数
DELETE FROM customer_journey_state WHERE subject_type='CUSTOMER';

-- 标签：确保存在
INSERT IGNORE INTO customer_tag (tenant_id, tag_code, tag_name, category, sort, del_flag, create_time, update_time)
VALUES
('1','HIGH_VALUE','高价值客户','VALUE',1,'0',NOW(),NOW()),
('1','NEW','新客户','LIFECYCLE',2,'0',NOW(),NOW()),
('1','PRICE_SENSITIVE','价格敏感','BEHAVIOR',3,'0',NOW(),NOW()),
('1','WHATSAPP_USER','WhatsApp用户','CHANNEL',4,'0',NOW(),NOW()),
('1','SMS_USER','短信用户','CHANNEL',5,'0',NOW(),NOW());

-- 客户旅程状态（SALES_FUNNEL 旅程）
-- 1 Alice Mwanza：意向客户，第2天
INSERT INTO customer_journey_state (tenant_id, journey_code, subject_type, subject_id, current_stage_code, current_stage_order, enter_stage_time, advance_count, create_time, update_time)
VALUES ('T001','SALES_FUNNEL','CUSTOMER',1,'STAGE_INTENTION',10,DATE_SUB(NOW(),INTERVAL 2 DAY),1,NOW(),NOW());
-- 2 Bob Banda：拜访演示，第7天
INSERT INTO customer_journey_state (tenant_id, journey_code, subject_type, subject_id, current_stage_code, current_stage_order, enter_stage_time, advance_count, create_time, update_time)
VALUES ('T001','SALES_FUNNEL','CUSTOMER',2,'STAGE_DEMO',20,DATE_SUB(NOW(),INTERVAL 7 DAY),2,NOW(),NOW());
-- 3 Carol Phiri：方案报价，第15天
INSERT INTO customer_journey_state (tenant_id, journey_code, subject_type, subject_id, current_stage_code, current_stage_order, enter_stage_time, advance_count, create_time, update_time)
VALUES ('T001','SALES_FUNNEL','CUSTOMER',3,'STAGE_PROPOSAL',30,DATE_SUB(NOW(),INTERVAL 15 DAY),3,NOW(),NOW());
-- 4 David Zulu：价格谈判，第30天
INSERT INTO customer_journey_state (tenant_id, journey_code, subject_type, subject_id, current_stage_code, current_stage_order, enter_stage_time, advance_count, create_time, update_time)
VALUES ('T001','SALES_FUNNEL','CUSTOMER',4,'STAGE_PRICE',40,DATE_SUB(NOW(),INTERVAL 30 DAY),4,NOW(),NOW());
-- 5 Emma Tembo：已签约，第40天
INSERT INTO customer_journey_state (tenant_id, journey_code, subject_type, subject_id, current_stage_code, current_stage_order, enter_stage_time, advance_count, create_time, update_time)
VALUES ('T001','SALES_FUNNEL','CUSTOMER',5,'STAGE_SIGNED',50,DATE_SUB(NOW(),INTERVAL 40 DAY),5,NOW(),NOW());
-- 6 Frank Ngoma：意向客户，第1天（截图里点开的就是他）
INSERT INTO customer_journey_state (tenant_id, journey_code, subject_type, subject_id, current_stage_code, current_stage_order, enter_stage_time, advance_count, create_time, update_time)
VALUES ('T001','SALES_FUNNEL','CUSTOMER',6,'STAGE_INTENTION',10,DATE_SUB(NOW(),INTERVAL 1 DAY),1,NOW(),NOW());

-- 客户标签关系
INSERT IGNORE INTO customer_tag_rel (tenant_id, customer_id, tag_code, create_time, update_time)
VALUES
('T001',1,'NEW',NOW(),NOW()),
('T001',1,'WHATSAPP_USER',NOW(),NOW()),
('T001',2,'HIGH_VALUE',NOW(),NOW()),
('T001',2,'WHATSAPP_USER',NOW(),NOW()),
('T001',3,'HIGH_VALUE',NOW(),NOW()),
('T001',3,'PRICE_SENSITIVE',NOW(),NOW()),
('T001',4,'PRICE_SENSITIVE',NOW(),NOW()),
('T001',4,'SMS_USER',NOW(),NOW()),
('T001',5,'HIGH_VALUE',NOW(),NOW()),
('T001',5,'WHATSAPP_USER',NOW(),NOW()),
('T001',6,'NEW',NOW(),NOW()),
('T001',6,'WHATSAPP_USER',NOW(),NOW());
