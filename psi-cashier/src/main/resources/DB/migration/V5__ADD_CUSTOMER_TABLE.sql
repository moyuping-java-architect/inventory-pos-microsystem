-- ===========================================================================
-- 客户表：存储从后台同步的客户数据，POS收银时用于选择客户、挂账、会员转化等
-- 数据来源：从中间件同步下载（下行同步）
-- ===========================================================================
CREATE TABLE customer (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    data_uuid       VARCHAR(64) NOT NULL,
    tenant_id       VARCHAR(50) NOT NULL,
    customer_code   VARCHAR(50) NOT NULL,
    customer_name   VARCHAR(200) NOT NULL,
    short_name      VARCHAR(100),
    contact_name    VARCHAR(100),
    contact_phone   VARCHAR(20),
    email           VARCHAR(100),
    address         VARCHAR(500),
    province        VARCHAR(50),
    city            VARCHAR(50),
    district        VARCHAR(50),
    zip_code        VARCHAR(10),
    tax_no          VARCHAR(50),
    bank_name       VARCHAR(100),
    bank_account    VARCHAR(50),
    customer_type   VARCHAR(10) DEFAULT '1',
    customer_level  VARCHAR(10),
    credit_limit    REAL DEFAULT 0.00,
    remark          VARCHAR(500),
    del_flag        INTEGER DEFAULT 0,
    status          INTEGER DEFAULT 1,
    create_time     VARCHAR(20),
    update_time     VARCHAR(20),

    UNIQUE(data_uuid),
    UNIQUE(tenant_id, customer_code)
);