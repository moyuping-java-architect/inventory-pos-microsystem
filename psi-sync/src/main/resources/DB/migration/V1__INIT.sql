-- 下行：进销存→POS
CREATE TABLE IF NOT EXISTS `down_sync` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `batch_uuid` VARCHAR(64) NOT NULL COMMENT '整批批次唯一UUID',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '租户ID',
    `shop_code` VARCHAR(50) COMMENT '商铺编码',
    `table_name` VARCHAR(50) NOT NULL COMMENT '目标业务表名',
    `json_data` TEXT NOT NULL COMMENT '批量明细JSON List',
    `sync_status` TINYINT DEFAULT 0 COMMENT '0待下载 1已下载',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入中间表自动生成时间(关键增量字段)',
    `last_download_time` DATETIME NULL COMMENT 'POS拉取完毕时间',
    UNIQUE INDEX `uk_batch_uuid` (`batch_uuid`) COMMENT '防同一批次重复插入',
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_shop_code` (`shop_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='下行同步中间表';

-- 上行：POS→进销存
CREATE TABLE IF NOT EXISTS `up_sync` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `batch_uuid` VARCHAR(64) NOT NULL COMMENT '批次唯一编号',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '租户ID',
    `shop_code` VARCHAR(50) COMMENT '商铺编码',
    `pos_sn` VARCHAR(100) NOT NULL COMMENT '收银机设备编码',
    `table_name` VARCHAR(50) NOT NULL COMMENT '单据对应表名',
    `json_data` TEXT NOT NULL COMMENT '单据集合JSON',
    `sync_status` TINYINT DEFAULT 0 COMMENT '0待处理 1成功 2失败',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '单据写入中间表时间',
    UNIQUE INDEX `uk_batch_uuid` (`batch_uuid`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_shop_code` (`shop_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上行单据中间表';