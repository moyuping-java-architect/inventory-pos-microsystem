-- 促销活动表（收银端本地副本，通过 psi-sync 从服务端同步）
CREATE TABLE IF NOT EXISTS promotion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data_uuid TEXT,
    tenant_id TEXT,
    promotion_no TEXT NOT NULL,
    promotion_name TEXT NOT NULL,
    promotion_type INTEGER DEFAULT 1,  -- 1:满减 2:满件折扣 3:买赠
    discount_type INTEGER DEFAULT 1,   -- 1:固定金额 2:折扣百分比
    discount_value REAL,
    min_amount REAL,
    min_quantity REAL,
    start_time TEXT,
    end_time TEXT,
    scope_type INTEGER DEFAULT 1,      -- 1:全部商品 2:指定商品 3:指定分类
    status INTEGER DEFAULT 1,
    priority INTEGER DEFAULT 0,
    superimposable INTEGER DEFAULT 0,  -- 0:不可叠加 1:可叠加
    remark TEXT,
    create_time TEXT,
    update_time TEXT
);

CREATE INDEX IF NOT EXISTS idx_promotion_status ON promotion(status);
CREATE INDEX IF NOT EXISTS idx_promotion_time ON promotion(start_time, end_time);

-- 促销活动商品范围表
CREATE TABLE IF NOT EXISTS promotion_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data_uuid TEXT,
    tenant_id TEXT,
    promotion_id INTEGER NOT NULL,
    promotion_no TEXT,
    item_type INTEGER DEFAULT 1,       -- 1:商品 2:分类
    item_code TEXT,
    item_name TEXT,
    category_code TEXT,
    category_name TEXT,
    create_time TEXT,
    update_time TEXT
);

CREATE INDEX IF NOT EXISTS idx_promotion_item_pid ON promotion_item(promotion_id);
CREATE INDEX IF NOT EXISTS idx_promotion_item_code ON promotion_item(item_code);
