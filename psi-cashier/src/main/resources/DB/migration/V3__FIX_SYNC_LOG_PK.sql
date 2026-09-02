-- ========================================================
-- P0 优先级：修复 sync_log 表重复 PRIMARY KEY 语法错误
-- SQLite 不支持 ALTER TABLE DROP CONSTRAINT，需要重建表
-- ========================================================

-- 1. 创建新表（正确的复合主键）
CREATE TABLE IF NOT EXISTS sync_log_new (
    tenant_id          VARCHAR(50) NOT NULL,
    type               VARCHAR(10) NOT NULL,
    last_download_time VARCHAR(20),
    PRIMARY KEY (tenant_id, type)
);

-- 2. 迁移历史数据
INSERT OR IGNORE INTO sync_log_new (tenant_id, type, last_download_time)
SELECT tenant_id, type, last_download_time FROM sync_log;

-- 3. 删除旧表
DROP TABLE IF EXISTS sync_log;

-- 4. 重命名新表
ALTER TABLE sync_log_new RENAME TO sync_log;
