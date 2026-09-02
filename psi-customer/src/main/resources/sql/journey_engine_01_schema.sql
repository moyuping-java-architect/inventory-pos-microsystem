-- ============================================================
-- PSI 客户旅程引擎 v2 - 零代码可配置版本 · 建表脚本
-- ============================================================
-- 设计主线：
--   业务事件（统一入口） → 触点生成规则 → 客户触点（事实层）
--   → 旅程阶段匹配 → 客户旅程状态（结论层，一客一旅程一条）
--
-- 老板可零代码配置的部分：字典、规则、旅程、阶段
-- 需要改代码的部分：接入一个全新的业务动作、新增一个指标维度
-- ============================================================

-- ------------------------------------------------------------
-- 1. journey_dict —— 通用字典（触点类型 / 意图 / 渠道）
--    配置页所有下拉框的数据源，统一三套打架的取值
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `journey_dict` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`    VARCHAR(64)  DEFAULT NULL              COMMENT '租户ID，NULL 表示全局内置',
    `dict_type`    VARCHAR(32)  NOT NULL                  COMMENT 'TOUCHPOINT_TYPE-触点类型 / INTENT-客户意图 / CHANNEL-接触渠道',
    `dict_code`    VARCHAR(64)  NOT NULL                  COMMENT '编码，写入触点表的实际值',
    `dict_name`    VARCHAR(100) NOT NULL                  COMMENT '显示名称',
    `subject_type` VARCHAR(16)  DEFAULT 'BOTH'            COMMENT '适用主体 CUSTOMER / MEMBER / BOTH',
    `extra_json`   TEXT                                   COMMENT '附加属性（图标、颜色等）',
    `built_in`     TINYINT      DEFAULT 0                 COMMENT '1-出厂内置不可删 0-用户自建',
    `sort_order`   INT          DEFAULT 0                 COMMENT '显示顺序',
    `enabled`      TINYINT      DEFAULT 1                 COMMENT '1-启用 0-停用',
    `del_flag`     TINYINT      DEFAULT 0                 COMMENT '0-正常 1-已删除',
    `create_time`  VARCHAR(32)  DEFAULT NULL,
    `update_time`  VARCHAR(32)  DEFAULT NULL,
    UNIQUE KEY `uk_dict` (`tenant_id`, `dict_type`, `dict_code`),
    INDEX `idx_dict_type` (`dict_type`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户旅程通用字典';

-- ------------------------------------------------------------
-- 2. business_event_dict —— 业务事件字典（穷举清单）
--    系统能发生的业务动作全集，配置页从这里下拉选
--    这张表的内容由开发维护，老板只读不写
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `business_event_dict` (
    `id`             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `event_code`     VARCHAR(64)  NOT NULL                COMMENT '事件编码 如 SALE.ORDER_APPROVED',
    `event_name`     VARCHAR(100) NOT NULL                COMMENT '事件中文名',
    `module_name`    VARCHAR(32)  NOT NULL                COMMENT '所属模块 SALE / CASHIER / MEMBER / FINANCE / CUSTOMER',
    `subject_type`   VARCHAR(16)  NOT NULL                COMMENT '主体类型 CUSTOMER-B2B客户 / MEMBER-B2C会员',
    `payload_fields` TEXT                                 COMMENT 'JSON数组，该事件能提供的字段，供条件构建器提示',
    `implemented`    TINYINT      DEFAULT 0               COMMENT '1-已接入总线 0-占位未接入（配置页灰显）',
    `sort_order`     INT          DEFAULT 0,
    `enabled`        TINYINT      DEFAULT 1,
    `create_time`    VARCHAR(32)  DEFAULT NULL,
    `update_time`    VARCHAR(32)  DEFAULT NULL,
    UNIQUE KEY `uk_event_code` (`event_code`),
    INDEX `idx_module` (`module_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务事件字典（穷举全集）';

-- ------------------------------------------------------------
-- 3. customer_metrics —— 客户/会员聚合指标
--    规则条件的可选字段 = 这张表的列，所以列要一次设计够
--    数据来源：复用 CustomerJourneyMapper 现有聚合 SQL，事件到达时增量刷新
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `customer_metrics` (
    `id`                  BIGINT        PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`           VARCHAR(64)   DEFAULT NULL,
    `subject_type`        VARCHAR(16)   NOT NULL          COMMENT 'CUSTOMER / MEMBER',
    `subject_id`          BIGINT        NOT NULL          COMMENT '客户ID 或 会员ID',
    `total_amount`        DECIMAL(18,2) DEFAULT 0         COMMENT '累计消费金额',
    `order_count`         INT           DEFAULT 0         COMMENT '累计订单数',
    `first_order_time`    VARCHAR(32)   DEFAULT NULL      COMMENT '首单时间',
    `last_order_time`     VARCHAR(32)   DEFAULT NULL      COMMENT '末单时间',
    `avg_order_amount`    DECIMAL(18,2) DEFAULT 0         COMMENT '平均客单价',
    `max_order_amount`    DECIMAL(18,2) DEFAULT 0         COMMENT '最大单笔金额',
    `last_order_amount`   DECIMAL(18,2) DEFAULT 0         COMMENT '最近一单金额',
    `avg_interval_days`   DECIMAL(10,2) DEFAULT 0         COMMENT '平均购买间隔天数',
    `days_since_last`     INT           DEFAULT 0         COMMENT '距上次消费天数',
    `return_count`        INT           DEFAULT 0         COMMENT '退货次数',
    `return_amount`       DECIMAL(18,2) DEFAULT 0         COMMENT '累计退货金额',
    `unpaid_amount`       DECIMAL(18,2) DEFAULT 0         COMMENT '当前欠款金额',
    `touchpoint_count`    INT           DEFAULT 0         COMMENT '累计触点数',
    `last_touchpoint_time` VARCHAR(32)  DEFAULT NULL      COMMENT '最近触点时间',
    `member_level`        VARCHAR(32)   DEFAULT NULL      COMMENT '当前会员等级（MEMBER主体用）',
    `points`              INT           DEFAULT 0         COMMENT '当前积分（MEMBER主体用）',
    `create_time`         VARCHAR(32)   DEFAULT NULL,
    `update_time`         VARCHAR(32)   DEFAULT NULL,
    UNIQUE KEY `uk_subject` (`tenant_id`, `subject_type`, `subject_id`),
    INDEX `idx_last_order` (`last_order_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户/会员聚合指标（规则条件数据源）';

-- ------------------------------------------------------------
-- 4. touchpoint_generate_rule —— 触点生成规则
--    老板配「什么事件 + 什么条件 → 生成什么触点」
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `touchpoint_generate_rule` (
    `id`               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`        VARCHAR(64)  DEFAULT NULL,
    `rule_name`        VARCHAR(100) NOT NULL              COMMENT '规则名称，如「累计满5000升银卡」',
    `event_code`       VARCHAR(64)  NOT NULL              COMMENT '监听的业务事件，取自 business_event_dict',
    `condition_json`   TEXT                               COMMENT 'JSON数组 [{"field":"totalAmount","op":"GE","value":"5000"}]，多条件 AND',
    `touchpoint_type`  VARCHAR(32)  NOT NULL              COMMENT '生成的触点类型，取自 journey_dict',
    `intent`           VARCHAR(32)  DEFAULT NULL          COMMENT '生成的触点意图',
    `channel`          VARCHAR(32)  DEFAULT 'SYSTEM'      COMMENT '生成的触点渠道',
    `summary_template` VARCHAR(500) DEFAULT NULL          COMMENT '摘要模板，支持 {docNo} {amount} {level} 占位符',
    `once_only`        TINYINT      DEFAULT 0             COMMENT '1-每个客户只触发一次（如会员升级）0-每次满足都触发',
    `enabled`          TINYINT      DEFAULT 1,
    `sort_order`       INT          DEFAULT 0,
    `del_flag`         TINYINT      DEFAULT 0,
    `create_time`      VARCHAR(32)  DEFAULT NULL,
    `update_time`      VARCHAR(32)  DEFAULT NULL,
    INDEX `idx_event` (`event_code`, `enabled`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='触点生成规则（事件→触点）';

-- ------------------------------------------------------------
-- 5. journey_rule_fire_log —— 规则触发记录（幂等控制）
--    once_only 规则 biz_key 固定写 'ONCE'，靠唯一索引挡住重复
--    普通规则 biz_key 写业务单号，防同一单据重复消费
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `journey_rule_fire_log` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`    VARCHAR(64)  DEFAULT NULL,
    `rule_id`      BIGINT       NOT NULL,
    `subject_type` VARCHAR(16)  NOT NULL,
    `subject_id`   BIGINT       NOT NULL,
    `biz_key`      VARCHAR(128) NOT NULL                  COMMENT 'once_only=1 时固定 ONCE；否则写业务单号',
    `touchpoint_id` BIGINT      DEFAULT NULL              COMMENT '生成的触点ID',
    `fire_time`    VARCHAR(32)  DEFAULT NULL,
    UNIQUE KEY `uk_fire` (`tenant_id`, `rule_id`, `subject_type`, `subject_id`, `biz_key`),
    INDEX `idx_subject` (`subject_type`, `subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则触发幂等记录';

-- ------------------------------------------------------------
-- 6. customer_journey_template —— 旅程模板（多套并行）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `customer_journey_template` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`    VARCHAR(64)  DEFAULT NULL,
    `journey_code` VARCHAR(32)  NOT NULL                  COMMENT '旅程编码 SALES_FUNNEL / MEMBERSHIP',
    `journey_name` VARCHAR(100) NOT NULL                  COMMENT '旅程名称',
    `subject_type` VARCHAR(16)  NOT NULL DEFAULT 'CUSTOMER' COMMENT '主体类型 CUSTOMER-按客户聚合 / MEMBER-按会员聚合',
    `icon`         VARCHAR(32)  DEFAULT NULL              COMMENT '旅程图标',
    `description`  VARCHAR(255) DEFAULT NULL              COMMENT '旅程说明',
    `is_default`   TINYINT      DEFAULT 0                 COMMENT '1-看板默认打开这一套',
    `enabled`      TINYINT      DEFAULT 1                 COMMENT '1-启用 0-停用（停用不在下拉出现但数据保留）',
    `sort_order`   INT          DEFAULT 0,
    `del_flag`     TINYINT      DEFAULT 0,
    `create_time`  VARCHAR(32)  DEFAULT NULL,
    `update_time`  VARCHAR(32)  DEFAULT NULL,
    UNIQUE KEY `uk_journey` (`tenant_id`, `journey_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户旅程模板（可多套）';

-- ------------------------------------------------------------
-- 7. customer_journey_stage —— 旅程阶段（归属某套旅程）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `customer_journey_stage` (
    `id`               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`        VARCHAR(64)  DEFAULT NULL,
    `journey_code`     VARCHAR(32)  NOT NULL              COMMENT '归属旅程',
    `stage_code`       VARCHAR(32)  NOT NULL              COMMENT '阶段编码',
    `stage_name`       VARCHAR(100) NOT NULL              COMMENT '阶段显示名',
    `color`            VARCHAR(16)  DEFAULT '#909399'     COMMENT '看板节点颜色',
    `icon`             VARCHAR(32)  DEFAULT 'User'        COMMENT '看板节点图标',
    `tip`              VARCHAR(255) DEFAULT NULL          COMMENT '悬浮提示，给老板看的行动建议',
    `match_touchpoint` VARCHAR(32)  DEFAULT NULL          COMMENT '匹配的触点类型',
    `match_intent`     VARCHAR(32)  DEFAULT NULL          COMMENT '匹配的触点意图，NULL 表示不限',
    `sort_order`       INT          NOT NULL DEFAULT 0    COMMENT '阶段顺序，越大越靠后，防倒退依据',
    `allow_rollback`   TINYINT      DEFAULT 0             COMMENT '1-允许从更高阶段退回（如流失阶段）0-只进不退',
    `del_flag`         TINYINT      DEFAULT 0,
    `create_time`      VARCHAR(32)  DEFAULT NULL,
    `update_time`      VARCHAR(32)  DEFAULT NULL,
    UNIQUE KEY `uk_stage` (`tenant_id`, `journey_code`, `stage_code`),
    INDEX `idx_journey` (`journey_code`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户旅程阶段';

-- ------------------------------------------------------------
-- 8. customer_journey_state —— 客户在某套旅程中的当前状态
--    ★ 同一客户 + 同一旅程 只有一条数据，靠 uk_subject_journey 保证
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `customer_journey_state` (
    `id`                  BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`           VARCHAR(64)  DEFAULT NULL,
    `journey_code`        VARCHAR(32)  NOT NULL,
    `subject_type`        VARCHAR(16)  NOT NULL           COMMENT 'CUSTOMER / MEMBER',
    `subject_id`          BIGINT       NOT NULL,
    `current_stage_code`  VARCHAR(32)  NOT NULL           COMMENT '当前阶段',
    `current_stage_order` INT          NOT NULL DEFAULT 0 COMMENT '当前阶段顺序（冗余，比较防倒退用）',
    `enter_stage_time`    VARCHAR(32)  DEFAULT NULL       COMMENT '进入当前阶段的时间',
    `last_touchpoint_id`  BIGINT       DEFAULT NULL       COMMENT '把客户推到当前阶段的那条触点',
    `last_event_code`     VARCHAR(64)  DEFAULT NULL       COMMENT '触发本次推进的业务事件',
    `advance_count`       INT          DEFAULT 1          COMMENT '累计推进次数',
    `create_time`         VARCHAR(32)  DEFAULT NULL,
    `update_time`         VARCHAR(32)  DEFAULT NULL,
    UNIQUE KEY `uk_subject_journey` (`tenant_id`, `journey_code`, `subject_type`, `subject_id`),
    INDEX `idx_journey_stage` (`journey_code`, `current_stage_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户旅程当前状态（一客一旅程一条）';
