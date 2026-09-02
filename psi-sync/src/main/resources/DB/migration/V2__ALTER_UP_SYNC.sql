-- 修改上行同步表，允许 pos_sn 为空
ALTER TABLE `up_sync` 
    MODIFY COLUMN `pos_sn` VARCHAR(100) COMMENT '收银机设备编码';

-- 添加处理时间字段
ALTER TABLE `up_sync` 
    ADD COLUMN `process_time` DATETIME NULL COMMENT '处理时间';

-- 添加错误信息字段
ALTER TABLE `up_sync` 
    ADD COLUMN `error_msg` TEXT COMMENT '错误信息';

-- 添加重试次数字段
ALTER TABLE `up_sync` 
    ADD COLUMN `retry_count` INT DEFAULT 0 COMMENT '重试次数';

-- 更新索引
ALTER TABLE `up_sync` 
    ADD INDEX `idx_sync_status` (`sync_status`),
    ADD INDEX `idx_create_time` (`create_time`);