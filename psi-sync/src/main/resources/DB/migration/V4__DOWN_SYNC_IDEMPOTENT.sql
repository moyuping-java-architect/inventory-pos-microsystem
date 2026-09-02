-- ========================================================
-- P1 优先级：下行同步表幂等性改造
-- 1. 添加 data_uuid 字段（业务数据唯一标识）
-- 2. 添加 data_version 字段（版本号冲突解决）
-- 3. 添加幂等索引（同一数据只保留最新版本）
-- ========================================================

-- 1. 添加 data_uuid 字段
ALTER TABLE `down_sync`
    ADD COLUMN `data_uuid` VARCHAR(64) NULL COMMENT '业务数据UUID' AFTER `table_name`;

-- 2. 添加 data_version 字段
ALTER TABLE `down_sync`
    ADD COLUMN `data_version` BIGINT DEFAULT 0 COMMENT '数据版本号' AFTER `data_uuid`;

-- 3. 为历史数据生成 data_uuid（基于 batch_uuid 兜底）
UPDATE `down_sync`
SET `data_uuid` = `batch_uuid`,
    `data_version` = 0
WHERE `data_uuid` IS NULL;

-- 4. 添加数据查询索引
ALTER TABLE `down_sync`
    ADD INDEX `idx_data_uuid` (`data_uuid`);

-- 5. 添加版本号查询索引
ALTER TABLE `down_sync`
    ADD INDEX `idx_data_version` (`data_version`);
