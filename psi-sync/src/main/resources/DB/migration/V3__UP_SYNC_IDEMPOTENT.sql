-- ========================================================
-- P0 优先级：上行同步表幂等性改造
-- 1. record_id 添加唯一索引（数据库级幂等约束）
-- 2. 添加 business_key 字段（业务主键，便于查询和冲突判断）
-- 3. 添加 data_version 字段（版本号冲突解决）
-- 4. 添加复合唯一索引（业务级幂等）
-- ========================================================

-- 1. 添加 business_key 字段
ALTER TABLE `up_sync`
    ADD COLUMN `business_key` VARCHAR(100) NULL COMMENT '业务主键（如订单号）' AFTER `table_name`;

-- 2. 添加 data_version 字段
ALTER TABLE `up_sync`
    ADD COLUMN `data_version` BIGINT DEFAULT 0 COMMENT '数据版本号（用于冲突解决）' AFTER `business_key`;

-- 3. 给已有数据的 record_id 补充唯一值（避免加唯一索引时冲突）
-- 注意：如果已有重复 record_id，需要先清理。以下脚本为历史数据生成确定性 record_id
UPDATE `up_sync`
SET `record_id` = CONCAT(COALESCE(`tenant_id`, 'default'), ':', `table_name`, ':', `batch_uuid`),
    `business_key` = `batch_uuid`
WHERE `record_id` IS NULL OR `record_id` = '';

-- 4. 添加 record_id 唯一索引（核心幂等约束）
ALTER TABLE `up_sync`
    ADD UNIQUE INDEX `uk_record_id` (`record_id`);

-- 5. 添加业务级复合唯一索引（同一业务数据只允许一条待处理记录）
ALTER TABLE `up_sync`
    ADD UNIQUE INDEX `uk_biz_pending` (`tenant_id`, `table_name`, `business_key`, `sync_status`);

-- 6. 添加业务键查询索引
ALTER TABLE `up_sync`
    ADD INDEX `idx_business_key` (`business_key`);
