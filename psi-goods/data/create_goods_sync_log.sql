-- 商品微服务同步日志表
-- 记录商品数据最后同步时间，用于增量同步
CREATE TABLE IF NOT EXISTS sync_log (
    data_uuid           VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）',
    tenant_id           VARCHAR(50) NOT NULL,
    type                VARCHAR(10) NOT NULL,
    last_download_time  VARCHAR(20) DEFAULT NULL COMMENT '最后同步时间',
    PRIMARY KEY (tenant_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步日志表';

