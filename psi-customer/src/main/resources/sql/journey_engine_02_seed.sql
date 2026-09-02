-- ============================================================
-- PSI 客户旅程引擎 v2 · 出厂种子数据
-- ============================================================

-- ############################################################
-- 一、journey_dict —— 统一字典（替代此前三套打架的取值）
-- ############################################################

-- --- 触点类型 · B2B 客户侧 ---
INSERT IGNORE INTO `journey_dict`
(`tenant_id`,`dict_type`,`dict_code`,`dict_name`,`subject_type`,`built_in`,`sort_order`,`enabled`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'TOUCHPOINT_TYPE','PROFILE_CREATED','客户建档','CUSTOMER',1,10,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','INQUIRY','问价咨询','CUSTOMER',1,20,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','WHATSAPP_MSG','WhatsApp 消息','CUSTOMER',1,30,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','PHONE_CALL','电话沟通','CUSTOMER',1,40,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','STORE_VISIT','到店/上门','CUSTOMER',1,50,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','DOC_SENT','发送方案报价','CUSTOMER',1,60,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','NEGOTIATION','议价洽谈','CUSTOMER',1,70,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','CONTRACT_SIGNED','合同签订','CUSTOMER',1,80,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','ORDER_COMPLETED','订单成交','CUSTOMER',1,90,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','ORDER_SHIPPED','出库发货','CUSTOMER',1,100,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','ORDER_RETURNED','退货','CUSTOMER',1,110,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','PAYMENT_RECEIVED','收到回款','CUSTOMER',1,120,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','DEBT_OVERDUE','欠款逾期','CUSTOMER',1,130,1,0,NOW(),NOW());

-- --- 触点类型 · B2C 会员侧 ---
INSERT IGNORE INTO `journey_dict`
(`tenant_id`,`dict_type`,`dict_code`,`dict_name`,`subject_type`,`built_in`,`sort_order`,`enabled`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'TOUCHPOINT_TYPE','MEMBER_REGISTERED','会员注册','MEMBER',1,200,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','FIRST_PURCHASE','首次消费','MEMBER',1,210,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','POS_CHECKOUT','收银结账','MEMBER',1,220,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','POS_REFUND','收银退款','MEMBER',1,230,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','MEMBER_RECHARGE','会员充值','MEMBER',1,240,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','MEMBER_UPGRADE','会员升级','MEMBER',1,250,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','POINTS_CHANGED','积分变动','MEMBER',1,260,1,0,NOW(),NOW());

-- --- 触点类型 · 通用 ---
INSERT IGNORE INTO `journey_dict`
(`tenant_id`,`dict_type`,`dict_code`,`dict_name`,`subject_type`,`built_in`,`sort_order`,`enabled`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'TOUCHPOINT_TYPE','COMPLAINT','投诉','BOTH',1,300,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','REFERRAL','转介绍','BOTH',1,310,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','CHURN_RISK','流失预警','BOTH',1,320,1,0,NOW(),NOW()),
(NULL,'TOUCHPOINT_TYPE','OTHER','其他','BOTH',1,999,1,0,NOW(),NOW());

-- --- 客户意图 ---
INSERT IGNORE INTO `journey_dict`
(`tenant_id`,`dict_type`,`dict_code`,`dict_name`,`subject_type`,`built_in`,`sort_order`,`enabled`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'INTENT','FIRST_CONTACT','初次接触','BOTH',1,10,1,0,NOW(),NOW()),
(NULL,'INTENT','INFO','咨询了解','BOTH',1,20,1,0,NOW(),NOW()),
(NULL,'INTENT','INTERESTED','有意向','BOTH',1,30,1,0,NOW(),NOW()),
(NULL,'INTENT','COMPARE','比价','BOTH',1,40,1,0,NOW(),NOW()),
(NULL,'INTENT','SCHEMA_DISCUSS','方案讨论','CUSTOMER',1,50,1,0,NOW(),NOW()),
(NULL,'INTENT','QUOTE_GIVEN','已报价','CUSTOMER',1,60,1,0,NOW(),NOW()),
(NULL,'INTENT','SIGNED','已签约','CUSTOMER',1,70,1,0,NOW(),NOW()),
(NULL,'INTENT','BUY','已购买','BOTH',1,80,1,0,NOW(),NOW()),
(NULL,'INTENT','RENEWED','复购续约','BOTH',1,90,1,0,NOW(),NOW()),
(NULL,'INTENT','COMPLAINT','投诉不满','BOTH',1,100,1,0,NOW(),NOW()),
(NULL,'INTENT','CHITCHAT','闲聊','BOTH',1,110,1,0,NOW(),NOW()),
(NULL,'INTENT','CHURN','流失','BOTH',1,120,1,0,NOW(),NOW());

-- --- 会员等级意图（会员升级触点的 intent 用这些值区分卡位）---
INSERT IGNORE INTO `journey_dict`
(`tenant_id`,`dict_type`,`dict_code`,`dict_name`,`subject_type`,`built_in`,`sort_order`,`enabled`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'INTENT','BRONZE','铜卡','MEMBER',1,200,1,0,NOW(),NOW()),
(NULL,'INTENT','SILVER','银卡','MEMBER',1,210,1,0,NOW(),NOW()),
(NULL,'INTENT','GOLD','金卡','MEMBER',1,220,1,0,NOW(),NOW()),
(NULL,'INTENT','BLACK','黑卡','MEMBER',1,230,1,0,NOW(),NOW());

-- --- 接触渠道 ---
INSERT IGNORE INTO `journey_dict`
(`tenant_id`,`dict_type`,`dict_code`,`dict_name`,`subject_type`,`built_in`,`sort_order`,`enabled`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'CHANNEL','SYSTEM','系统自动','BOTH',1,10,1,0,NOW(),NOW()),
(NULL,'CHANNEL','WHATSAPP','WhatsApp','BOTH',1,20,1,0,NOW(),NOW()),
(NULL,'CHANNEL','PHONE','电话','BOTH',1,30,1,0,NOW(),NOW()),
(NULL,'CHANNEL','IN_STORE','门店','BOTH',1,40,1,0,NOW(),NOW()),
(NULL,'CHANNEL','FACEBOOK','Facebook','BOTH',1,50,1,0,NOW(),NOW()),
(NULL,'CHANNEL','EMAIL','邮件','BOTH',1,60,1,0,NOW(),NOW()),
(NULL,'CHANNEL','OTHER','其他','BOTH',1,999,1,0,NOW(),NOW());


-- ############################################################
-- 二、business_event_dict —— 业务事件穷举（24 个）
--     implemented=1 表示已接入总线，=0 表示占位待接
-- ############################################################

-- --- 销售模块（主体：客户）---
INSERT IGNORE INTO `business_event_dict`
(`event_code`,`event_name`,`module_name`,`subject_type`,`payload_fields`,`implemented`,`sort_order`,`enabled`,`create_time`,`update_time`) VALUES
('SALE.ORDER_SUBMITTED','销售订单提交送审','SALE','CUSTOMER','["docNo","orderAmount","currency","itemCount"]',0,10,1,NOW(),NOW()),
('SALE.ORDER_APPROVED','销售订单审批通过','SALE','CUSTOMER','["docNo","orderAmount","currency","itemCount"]',1,20,1,NOW(),NOW()),
('SALE.ORDER_REJECTED','销售订单审批驳回','SALE','CUSTOMER','["docNo","orderAmount","rejectReason"]',0,30,1,NOW(),NOW()),
('SALE.ORDER_SHIPPED','销售出库发货','SALE','CUSTOMER','["docNo","orderAmount","shipTime"]',0,40,1,NOW(),NOW()),
('SALE.ORDER_RETURNED','销售退货','SALE','CUSTOMER','["docNo","returnAmount","returnReason"]',0,50,1,NOW(),NOW());

-- --- 财务回款模块（主体：客户）---
INSERT IGNORE INTO `business_event_dict`
(`event_code`,`event_name`,`module_name`,`subject_type`,`payload_fields`,`implemented`,`sort_order`,`enabled`,`create_time`,`update_time`) VALUES
('FINANCE.PAYMENT_RECEIVED','客户付款登记','FINANCE','CUSTOMER','["docNo","payAmount","payMethod","currency"]',0,110,1,NOW(),NOW()),
('FINANCE.DEBT_CHANGED','欠款状态变更','FINANCE','CUSTOMER','["debtAmount","changeAmount"]',0,120,1,NOW(),NOW()),
('FINANCE.CREDIT_EXCEEDED','超出信用额度','FINANCE','CUSTOMER','["creditLimit","currentDebt","exceedAmount"]',0,130,1,NOW(),NOW()),
('FINANCE.RECEIVABLE_OVERDUE','应收账款逾期','FINANCE','CUSTOMER','["docNo","overdueAmount","overdueDays"]',0,140,1,NOW(),NOW());

-- --- 客户建档模块（主体：客户）---
INSERT IGNORE INTO `business_event_dict`
(`event_code`,`event_name`,`module_name`,`subject_type`,`payload_fields`,`implemented`,`sort_order`,`enabled`,`create_time`,`update_time`) VALUES
('CUSTOMER.CREATED','客户建档','CUSTOMER','CUSTOMER','["customerName","customerCode","source"]',0,210,1,NOW(),NOW()),
('CUSTOMER.UPDATED','客户信息变更','CUSTOMER','CUSTOMER','["customerName","changedFields"]',0,220,1,NOW(),NOW()),
('CUSTOMER.LEVEL_CHANGED','客户等级变更','CUSTOMER','CUSTOMER','["oldLevel","newLevel"]',0,230,1,NOW(),NOW()),
('CUSTOMER.STATUS_CHANGED','客户停用启用','CUSTOMER','CUSTOMER','["oldStatus","newStatus"]',0,240,1,NOW(),NOW()),
('CUSTOMER.CONVERTED_TO_MEMBER','客户转为会员','CUSTOMER','CUSTOMER','["memberId","memberCode"]',0,250,1,NOW(),NOW());

-- --- 收银模块（主体：会员）---
INSERT IGNORE INTO `business_event_dict`
(`event_code`,`event_name`,`module_name`,`subject_type`,`payload_fields`,`implemented`,`sort_order`,`enabled`,`create_time`,`update_time`) VALUES
('CASHIER.CHECKOUT_COMPLETED','收银结账完成','CASHIER','MEMBER','["orderNo","orderAmount","payMethod","itemCount","currency"]',0,310,1,NOW(),NOW()),
('CASHIER.REFUNDED','收银退款','CASHIER','MEMBER','["orderNo","refundAmount","refundReason"]',0,320,1,NOW(),NOW()),
('CASHIER.FULL_REFUNDED','整单退完','CASHIER','MEMBER','["orderNo","refundAmount"]',0,330,1,NOW(),NOW()),
('CASHIER.ORDER_HELD','挂单','CASHIER','MEMBER','["orderNo","orderAmount"]',0,340,1,NOW(),NOW()),
('CASHIER.ORDER_RESUMED','取单','CASHIER','MEMBER','["orderNo","orderAmount"]',0,350,1,NOW(),NOW());

-- --- 会员模块（主体：会员）---
INSERT IGNORE INTO `business_event_dict`
(`event_code`,`event_name`,`module_name`,`subject_type`,`payload_fields`,`implemented`,`sort_order`,`enabled`,`create_time`,`update_time`) VALUES
('MEMBER.REGISTERED','会员注册','MEMBER','MEMBER','["memberCode","memberName","phone","source"]',0,410,1,NOW(),NOW()),
('MEMBER.RECHARGED','会员充值','MEMBER','MEMBER','["rechargeAmount","balanceAfter"]',0,420,1,NOW(),NOW()),
('MEMBER.CARD_CONSUMED','会员卡消费','MEMBER','MEMBER','["consumeAmount","balanceAfter"]',0,430,1,NOW(),NOW()),
('MEMBER.POINTS_CHANGED','积分增减','MEMBER','MEMBER','["pointsChange","pointsAfter","reason"]',0,440,1,NOW(),NOW()),
('MEMBER.LEVEL_UPGRADED','会员等级升级','MEMBER','MEMBER','["oldLevel","newLevel","upgradeReason"]',0,450,1,NOW(),NOW());


-- ############################################################
-- 三、customer_journey_template —— 出厂两套旅程
-- ############################################################
INSERT IGNORE INTO `customer_journey_template`
(`tenant_id`,`journey_code`,`journey_name`,`subject_type`,`icon`,`description`,`is_default`,`enabled`,`sort_order`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'SALES_FUNNEL','销售漏斗','CUSTOMER','Sell','B2B 大客户从初次接触到复购的完整跟进链路',1,1,10,0,NOW(),NOW()),
(NULL,'MEMBERSHIP','会员体系','MEMBER','Medal','B2C 零售会员从注册到黑卡的成长路径',0,1,20,0,NOW(),NOW());


-- ############################################################
-- 四、customer_journey_stage —— 两套旅程的阶段
-- ############################################################

-- --- 销售漏斗 6 阶段（沿用原 JOURNEY_STAGES 定义）---
INSERT IGNORE INTO `customer_journey_stage`
(`tenant_id`,`journey_code`,`stage_code`,`stage_name`,`color`,`icon`,`tip`,`match_touchpoint`,`match_intent`,`sort_order`,`allow_rollback`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'SALES_FUNNEL','STAGE_INTENTION','阶段1: 培育介绍','#909399','User','刚建立联系，先摸清对方需求和预算','INQUIRY','FIRST_CONTACT',10,0,0,NOW(),NOW()),
(NULL,'SALES_FUNNEL','STAGE_DEMO','阶段2: 需求确认','#5D8FF0','VideoCamera','已表达兴趣，安排到店或上门演示','STORE_VISIT','INTERESTED',20,0,0,NOW(),NOW()),
(NULL,'SALES_FUNNEL','STAGE_PROPOSAL','阶段3: 方案讨论','#40C9C6','Document','方案已发出，主动约时间过一遍细节','DOC_SENT','SCHEMA_DISCUSS',30,0,0,NOW(),NOW()),
(NULL,'SALES_FUNNEL','STAGE_PRICE','阶段4: 报价议价','#F9CB40','Money','报价已给，盯紧别让对手截胡','NEGOTIATION','QUOTE_GIVEN',40,0,0,NOW(),NOW()),
(NULL,'SALES_FUNNEL','STAGE_SIGNED','阶段5: 签约成交','#32CD32','EditPen','已成交，确保交付顺畅为复购铺路','CONTRACT_SIGNED','SIGNED',50,0,0,NOW(),NOW()),
(NULL,'SALES_FUNNEL','STAGE_RENEWED','阶段6: 客户成功','#67C23A','Star','已复购的老客户，最容易转介绍','ORDER_COMPLETED','RENEWED',60,0,0,NOW(),NOW());

-- --- 会员体系 6 卡位（触点全部走会员升级类）---
INSERT IGNORE INTO `customer_journey_stage`
(`tenant_id`,`journey_code`,`stage_code`,`stage_name`,`color`,`icon`,`tip`,`match_touchpoint`,`match_intent`,`sort_order`,`allow_rollback`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'MEMBERSHIP','MEMBER_NEW','新注册会员','#909399','User','刚注册还没消费，推一张首单优惠券','MEMBER_REGISTERED',NULL,10,0,0,NOW(),NOW()),
(NULL,'MEMBERSHIP','MEMBER_FIRST_BUY','首次消费','#5D8FF0','ShoppingCart','已开单，30 天内再来一次就留住了','FIRST_PURCHASE',NULL,20,0,0,NOW(),NOW()),
(NULL,'MEMBERSHIP','MEMBER_BRONZE','铜卡会员','#CD7F32','Medal','稳定消费中，介绍升级门槛促进客单价','MEMBER_UPGRADE','BRONZE',30,0,0,NOW(),NOW()),
(NULL,'MEMBERSHIP','MEMBER_SILVER','银卡会员','#A8A8A8','Medal','已是常客，可推专属折扣和生日礼','MEMBER_UPGRADE','SILVER',40,0,0,NOW(),NOW()),
(NULL,'MEMBERSHIP','MEMBER_GOLD','金卡会员','#E6A23C','Trophy','高价值客户，值得单独维护关系','MEMBER_UPGRADE','GOLD',50,0,0,NOW(),NOW()),
(NULL,'MEMBERSHIP','MEMBER_BLACK','黑卡会员','#303133','Star','顶级客户，老板本人应该认识他','MEMBER_UPGRADE','BLACK',60,0,0,NOW(),NOW());


-- ############################################################
-- 五、touchpoint_generate_rule —— 出厂预置规则
--     condition_json 运算符：GE >= / GT > / LE <= / LT < / EQ = / NE != / IN / CONTAINS
--     可用字段 = customer_metrics 列的驼峰名 + 事件 payload 字段
-- ############################################################

-- --- 销售漏斗规则 ---
INSERT IGNORE INTO `touchpoint_generate_rule`
(`tenant_id`,`rule_name`,`event_code`,`condition_json`,`touchpoint_type`,`intent`,`channel`,`summary_template`,`once_only`,`enabled`,`sort_order`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'销售订单成交','SALE.ORDER_APPROVED',NULL,'ORDER_COMPLETED','BUY','SYSTEM','订单 {docNo} 成交，金额 {orderAmount} {currency}',0,1,10,0,NOW(),NOW()),
(NULL,'大额订单视为签约','SALE.ORDER_APPROVED','[{"field":"orderAmount","op":"GE","value":"3000"}]','CONTRACT_SIGNED','SIGNED','SYSTEM','大额订单 {docNo} 签约，金额 {orderAmount}',0,1,20,0,NOW(),NOW()),
(NULL,'二次下单视为复购','SALE.ORDER_APPROVED','[{"field":"orderCount","op":"GE","value":"2"}]','ORDER_COMPLETED','RENEWED','SYSTEM','客户复购，累计 {orderCount} 单',0,1,30,0,NOW(),NOW()),
(NULL,'客户建档','CUSTOMER.CREATED',NULL,'PROFILE_CREATED','FIRST_CONTACT','SYSTEM','客户 {customerName} 建档',1,1,40,0,NOW(),NOW()),
(NULL,'收到客户回款','FINANCE.PAYMENT_RECEIVED',NULL,'PAYMENT_RECEIVED','BUY','SYSTEM','收到回款 {payAmount}，单号 {docNo}',0,1,50,0,NOW(),NOW()),
(NULL,'应收逾期预警','FINANCE.RECEIVABLE_OVERDUE','[{"field":"overdueDays","op":"GE","value":"30"}]','DEBT_OVERDUE','COMPLAINT','SYSTEM','应收逾期 {overdueDays} 天，金额 {overdueAmount}',0,1,60,0,NOW(),NOW());

-- --- 会员体系规则（阈值按赞比亚零售场景设定，单位 ZMW）---
INSERT IGNORE INTO `touchpoint_generate_rule`
(`tenant_id`,`rule_name`,`event_code`,`condition_json`,`touchpoint_type`,`intent`,`channel`,`summary_template`,`once_only`,`enabled`,`sort_order`,`del_flag`,`create_time`,`update_time`) VALUES
(NULL,'会员注册','MEMBER.REGISTERED',NULL,'MEMBER_REGISTERED','FIRST_CONTACT','SYSTEM','会员 {memberName} 注册成功',1,1,110,0,NOW(),NOW()),
(NULL,'首次消费','CASHIER.CHECKOUT_COMPLETED','[{"field":"orderCount","op":"EQ","value":"1"}]','FIRST_PURCHASE','BUY','SYSTEM','首次消费 {orderAmount}，单号 {orderNo}',1,1,120,0,NOW(),NOW()),
(NULL,'累计满 1000 升铜卡','CASHIER.CHECKOUT_COMPLETED','[{"field":"totalAmount","op":"GE","value":"1000"}]','MEMBER_UPGRADE','BRONZE','SYSTEM','累计消费 {totalAmount}，升级铜卡',1,1,130,0,NOW(),NOW()),
(NULL,'累计满 5000 升银卡','CASHIER.CHECKOUT_COMPLETED','[{"field":"totalAmount","op":"GE","value":"5000"}]','MEMBER_UPGRADE','SILVER','SYSTEM','累计消费 {totalAmount}，升级银卡',1,1,140,0,NOW(),NOW()),
(NULL,'累计满 20000 升金卡','CASHIER.CHECKOUT_COMPLETED','[{"field":"totalAmount","op":"GE","value":"20000"}]','MEMBER_UPGRADE','GOLD','SYSTEM','累计消费 {totalAmount}，升级金卡',1,1,150,0,NOW(),NOW()),
(NULL,'累计满 50000 升黑卡','CASHIER.CHECKOUT_COMPLETED','[{"field":"totalAmount","op":"GE","value":"50000"}]','MEMBER_UPGRADE','BLACK','SYSTEM','累计消费 {totalAmount}，升级黑卡',1,1,160,0,NOW(),NOW()),
(NULL,'手工调整会员等级','MEMBER.LEVEL_UPGRADED',NULL,'MEMBER_UPGRADE','{newLevel}','SYSTEM','会员等级由 {oldLevel} 调整为 {newLevel}',0,1,170,0,NOW(),NOW());
