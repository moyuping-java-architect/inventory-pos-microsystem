-- 添加 recordId 字段用于幂等性校验
ALTER TABLE `up_sync` ADD COLUMN `record_id` VARCHAR(64) COMMENT '记录唯一ID（用于幂等性校验）';

-- 创建 recordId 索引，提高查询性能
CREATE INDEX `idx_record_id` ON `up_sync` (`record_id`);