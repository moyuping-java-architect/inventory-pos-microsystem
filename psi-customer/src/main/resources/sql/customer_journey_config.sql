-- ============================================================
-- customer_journey_config: Customer journey zero-code config
-- All business rules (churn thresholds, message templates, etc.)
-- are stored here so shop owners can adjust without touching code
-- ============================================================

CREATE TABLE IF NOT EXISTS customer_journey_config (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    config_group  VARCHAR(50)  NOT NULL              COMMENT 'CHURN_MODEL / MESSAGE_TEMPLATE / TOUCHPOINT_TYPE / CHANNEL_RULE / SUGGESTED_ACTION / DASHBOARD',
    config_key    VARCHAR(100) NOT NULL              COMMENT 'Config key within group',
    config_value  TEXT                               COMMENT 'Config value (number as string, template text, option label, etc.)',
    value_type    VARCHAR(20)  DEFAULT 'STRING'      COMMENT 'NUMBER / STRING / BOOLEAN / JSON / OPTION',
    display_name  VARCHAR(100)                       COMMENT 'Form label for admin UI',
    description   VARCHAR(255)                       COMMENT 'Help text shown below the field',
    sort_order    INT          DEFAULT 0             COMMENT 'Display order within group',
    del_flag      INT          DEFAULT 0             COMMENT '0=active 1=deleted',
    create_time   VARCHAR(20)                        COMMENT 'yyyy-MM-dd HH:mm:ss',
    update_time   VARCHAR(20)                        COMMENT 'yyyy-MM-dd HH:mm:ss',
    UNIQUE KEY uk_group_key (config_group, config_key),
    INDEX idx_config_group (config_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Customer journey zero-code configuration';

-- ============================================================
-- Seed defaults (28 items across 6 groups)
-- ============================================================

-- --- CHURN_MODEL: adaptive churn thresholds ---
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('CHURN_MODEL', 'active_multiplier',     '1.5', 'NUMBER', 'Active threshold',   'Within this x normal cycle = ACTIVE',          1, 0, NOW(), NOW()),
('CHURN_MODEL', 'at_risk_multiplier',    '2.5', 'NUMBER', 'At-risk threshold',  'Within this x normal cycle = AT_RISK',         2, 0, NOW(), NOW()),
('CHURN_MODEL', 'high_risk_multiplier',  '4.0', 'NUMBER', 'High-risk threshold', 'Within this x normal cycle = HIGH_RISK',       3, 0, NOW(), NOW()),
('CHURN_MODEL', 'max_days_churned',      '90',  'NUMBER', 'Max days to churn',   'Absolute days before CHURNED regardless of cycle', 4, 0, NOW(), NOW()),
('CHURN_MODEL', 'new_customer_days',     '30',  'NUMBER', 'New customer days',   'Days below which a customer with <2 orders is NEW', 5, 0, NOW(), NOW()),
('CHURN_MODEL', 'fallback_active_days', '30',  'NUMBER', 'Fallback active days', 'Days for ACTIVE when no purchase history exists', 6, 0, NOW(), NOW()),
('CHURN_MODEL', 'fallback_at_risk_days', '60',  'NUMBER', 'Fallback at-risk days', 'Days for AT_RISK when no purchase history exists', 7, 0, NOW(), NOW());

-- --- MESSAGE_TEMPLATE: WhatsApp win-back templates with {name}/{days}/{product} placeholders ---
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('MESSAGE_TEMPLATE', 'template_at_risk',      'Hi {name}! We haven''t seen you in {days} days. Last time you bought {product}. Come back this week for 10% off! Reply STOP to opt out.', 'STRING', 'At-risk message',   'Sent to AT_RISK customers. Variables: {name} {days} {product}', 1, 0, NOW(), NOW()),
('MESSAGE_TEMPLATE', 'template_high_risk',    'Hi {name}! It''s been {days} days since your last visit. Is everything OK? We''d love to see you again. Special offer inside!', 'STRING', 'High-risk message',  'Sent to HIGH_RISK customers. Variables: {name} {days} {product}', 2, 0, NOW(), NOW()),
('MESSAGE_TEMPLATE', 'template_churned',       'Hi {name}! We miss you! It''s been {days} days. Here''s a special 15% off just for you. Valid this week only.', 'STRING', 'Churned message',    'Sent to CHURNED customers. Variables: {name} {days} {product}', 3, 0, NOW(), NOW()),
('MESSAGE_TEMPLATE', 'template_new_churned',  'Hi {name}! How was your first purchase of {product}? We''d love your feedback. Come back for 10% off your next order!', 'STRING', 'New-churned message', 'Sent to NEW_CHURNED customers. Variables: {name} {days} {product}', 4, 0, NOW(), NOW());

-- --- TOUCHPOINT_TYPE: non-purchase interaction types (dropdown options) ---
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('TOUCHPOINT_TYPE', 'WHATSAPP_INQUIRY',   'WhatsApp price inquiry', 'OPTION', 'WhatsApp inquiry',  'Customer asked price on WhatsApp', 1, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'STORE_VISIT',        'Store visit',            'OPTION', 'Store visit',       'Customer visited the shop',         2, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'PHONE_CALL',        'Phone call',             'OPTION', 'Phone call',         'Customer called by phone',           3, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'DELIVERY_FEEDBACK', 'Delivery feedback',      'OPTION', 'Delivery feedback',  'Feedback after delivery',            4, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'COMPLAINT',         'Complaint',              'OPTION', 'Complaint',          'Customer complained',               5, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'REFERRAL',          'Referral',               'OPTION', 'Referral',           'Customer referred someone',          6, 0, NOW(), NOW());

-- --- CHANNEL_RULE: which channel to use for win-back ---
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('CHANNEL_RULE', 'high_value_threshold', '5000',     'NUMBER', 'High-value threshold', 'Total spent above this = use PHONE channel', 1, 0, NOW(), NOW()),
('CHANNEL_RULE', 'default_channel',     'WHATSAPP', 'STRING', 'Default channel',      'Default win-back channel for normal customers', 2, 0, NOW(), NOW());

-- --- SUGGESTED_ACTION: what the dashboard suggests for each churn level ---
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('SUGGESTED_ACTION', 'action_at_risk',     'Send WhatsApp: We miss you! 10% off this week', 'STRING', 'At-risk action',    'Suggested action for AT_RISK',     1, 0, NOW(), NOW()),
('SUGGESTED_ACTION', 'action_high_risk',   'Call directly: Haven''t seen you, everything OK?', 'STRING', 'High-risk action',  'Suggested action for HIGH_RISK',    2, 0, NOW(), NOW()),
('SUGGESTED_ACTION', 'action_churned',     'Last attempt: Special 15% off just for you',    'STRING', 'Churned action',    'Suggested action for CHURNED',      3, 0, NOW(), NOW()),
('SUGGESTED_ACTION', 'action_new_churned', 'Follow up: How was your first purchase?',       'STRING', 'New-churned action', 'Suggested action for NEW_CHURNED',  4, 0, NOW(), NOW());

-- --- DASHBOARD: display limits ---
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('DASHBOARD', 'top_customers_limit',    '5',  'NUMBER', 'Top customers shown',   'How many top customers on dashboard', 1, 0, NOW(), NOW()),
('DASHBOARD', 'top_churn_alerts_limit',  '10', 'NUMBER', 'Churn alerts shown',    'How many churn alerts on dashboard',  2, 0, NOW(), NOW()),
('DASHBOARD', 'timeline_limit',         '20', 'NUMBER', 'Timeline items shown',   'Max timeline items per customer',     3, 0, NOW(), NOW()),
('DASHBOARD', 'winback_limit',          '20', 'NUMBER', 'Winback suggestions',    'Max win-back suggestions returned',    4, 0, NOW(), NOW());
