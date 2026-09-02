USE erp_system_db;

-- =============================================
-- 收银机配置表（下行同步源表）
-- 后台管理收银机，数据通过 psi-sync 下行同步到 POS 机本地 SQLite
-- =============================================
CREATE TABLE IF NOT EXISTS `pos_config` (
    `id`            BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid`     VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）',
    `tenant_id`     BIGINT DEFAULT 0 COMMENT '租户ID',
    `pos_sn`        VARCHAR(100) NOT NULL COMMENT 'POS硬件序列号(全球唯一)',
    `shop_code`     VARCHAR(50) NOT NULL COMMENT '门店编码',
    `pos_id`        VARCHAR(50) NOT NULL COMMENT '收银机编号(如POS01)',
    `pos_name`      VARCHAR(100) DEFAULT NULL COMMENT '收银机名称',
    `create_by`     BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `status`        TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_pos_sn` (`pos_sn`),
    UNIQUE KEY `uk_shop_pos_id` (`shop_code`, `pos_id`),
    INDEX `idx_shop_code` (`shop_code`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_del_flag` (`del_flag`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收银机配置表';

-- =============================================
-- 收银员表（下行同步源表）
-- 后台管理收银员，数据通过 psi-sync 下行同步到 POS 机本地 SQLite
-- =============================================
CREATE TABLE IF NOT EXISTS `pos_operator` (
    `id`            BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid`     VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）',
    `tenant_id`     BIGINT DEFAULT 0 COMMENT '租户ID',
    `shop_code`     VARCHAR(50) NOT NULL COMMENT '门店编码',
    `username`      VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password`      VARCHAR(255) NOT NULL COMMENT '登录密码',
    `real_name`     VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `role`          TINYINT DEFAULT 1 COMMENT '角色 1-收银员 2-管理员',
    `create_by`     BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `status`        TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_shop_username` (`shop_code`, `username`),
    INDEX `idx_shop_code` (`shop_code`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_del_flag` (`del_flag`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收银员表';