-- 话术库 * 销冠 模块表结构（旅程触达计划版）
-- 业务核心：客户在某旅程节点停留第 N 天 + 命中标签 + 未发送过 -> 触达对应话术

SET NAMES utf8mb4;

-- 1. 客户标签字典
CREATE TABLE IF NOT EXISTS customer_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL DEFAULT '0' COMMENT '租户ID',
    tag_code VARCHAR(64) NOT NULL COMMENT '标签编码（唯一业务键）',
    tag_name VARCHAR(128) NOT NULL COMMENT '标签名称',
    category VARCHAR(64) DEFAULT 'DEFAULT' COMMENT '分类：INDUSTRY/BUDGET/SOURCE/DEFAULT',
    color VARCHAR(32) DEFAULT '#409EFF' COMMENT '标签颜色',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time VARCHAR(32) DEFAULT NULL COMMENT '创建时间',
    update_time VARCHAR(32) DEFAULT NULL COMMENT '更新时间',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志 0-正常 1-删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_tag_code (tenant_id, tag_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签字典';

-- 2. 客户标签关系
CREATE TABLE IF NOT EXISTS customer_tag_rel (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL DEFAULT '0' COMMENT '租户ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID（关联 customer.id）',
    tag_code VARCHAR(64) NOT NULL COMMENT '标签编码',
    create_time VARCHAR(32) DEFAULT NULL COMMENT '创建时间',
    update_time VARCHAR(32) DEFAULT NULL COMMENT '更新时间',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志 0-正常 1-删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_customer_tag (tenant_id, customer_id, tag_code),
    KEY idx_customer_id (tenant_id, customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签关系';

-- 3. 销冠话术库（旅程触达话术）
-- 一条话术绑定：旅程节点 + 停留天数区间 + 标签条件
CREATE TABLE IF NOT EXISTS sales_script_library (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL DEFAULT '0' COMMENT '租户ID',
    script_code VARCHAR(128) NOT NULL COMMENT '话术编码（含版本，如：CONSIDERING_D1_V1）',
    script_name VARCHAR(256) NOT NULL COMMENT '话术名称',
    category VARCHAR(64) DEFAULT 'SALES' COMMENT '分类：ICE_BREAK/SOLUTION/CLOSE/REACTIVATE/SALES',
    content TEXT NOT NULL COMMENT '话术正文（支持占位符如 {{name}}/{{school}}/{{product}}）',
    channel VARCHAR(64) DEFAULT 'WHATSAPP' COMMENT '适用渠道：WHATSAPP/PHONE/IN_STORE/SMS/API',
    stage_code VARCHAR(64) NOT NULL COMMENT '匹配客户旅程阶段编码',
    stage_name VARCHAR(128) DEFAULT NULL COMMENT '阶段名称（展示用）',
    tags VARCHAR(512) DEFAULT NULL COMMENT '匹配标签编码集合，逗号分隔',
    tag_match_mode VARCHAR(16) DEFAULT 'ANY' COMMENT '标签匹配模式：ANY 任一命中 / ALL 全部命中',
    day_start INT NOT NULL DEFAULT 1 COMMENT '阶段停留天数开始（含）',
    day_end INT NOT NULL DEFAULT 1 COMMENT '阶段停留天数结束（含）',
    priority INT DEFAULT 0 COMMENT '优先级，数值越大越优先',
    send_limit INT DEFAULT 1 COMMENT '同一客户在该节点-天数区间的最大发送次数',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '启用 0-禁用 1-启用',
    create_time VARCHAR(32) DEFAULT NULL COMMENT '创建时间',
    update_time VARCHAR(32) DEFAULT NULL COMMENT '更新时间',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志 0-正常 1-删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_script_code (tenant_id, script_code),
    KEY idx_tenant_stage_days (tenant_id, stage_code, day_start, day_end, is_enabled, del_flag),
    KEY idx_tenant_category (tenant_id, category, is_enabled, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销冠话术库（旅程触达话术）';

-- 4. 话术发送记录（去重核心）
-- 保证同一客户在同一节点-天数区间不会重复收到同一条话术
CREATE TABLE IF NOT EXISTS customer_script_send_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL DEFAULT '0' COMMENT '租户ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    script_id BIGINT NOT NULL COMMENT '话术ID',
    stage_code VARCHAR(64) NOT NULL COMMENT '发送时客户所在节点',
    day_in_stage INT NOT NULL COMMENT '发送时客户在节点第几天',
    channel VARCHAR(64) DEFAULT 'WHATSAPP' COMMENT '发送渠道',
    send_status TINYINT DEFAULT 1 COMMENT '发送状态 0-失败 1-成功 2-待发送',
    send_time VARCHAR(32) DEFAULT NULL COMMENT '发送时间',
    operator_id VARCHAR(64) DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(128) DEFAULT NULL COMMENT '操作人姓名',
    remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
    create_time VARCHAR(32) DEFAULT NULL COMMENT '创建时间',
    update_time VARCHAR(32) DEFAULT NULL COMMENT '更新时间',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志 0-正常 1-删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_customer_script_day (tenant_id, customer_id, script_id, stage_code, day_in_stage),
    KEY idx_customer_id (tenant_id, customer_id, send_time),
    KEY idx_script_id (tenant_id, script_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话术发送记录';
