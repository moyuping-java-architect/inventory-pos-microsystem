-- ========================================================
-- V1: 收银系统基础表结构初始化
-- 包含：序号、同步日志、商品、会员、操作员、订单、退款、挂单、交班结算
-- ========================================================

-- 序号生成表
CREATE TABLE IF NOT EXISTS sys_seq (
    pos_id      VARCHAR(30) NOT NULL,
    seq_type    VARCHAR(20) NOT NULL,
    day         VARCHAR(8) NOT NULL,
    curr_no     INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (pos_id, seq_type, day)
);

-- 同步日志表
CREATE TABLE IF NOT EXISTS sync_log (
    tenant_id          VARCHAR(50) NOT NULL,
    type               VARCHAR(10) NOT NULL,
    last_download_time VARCHAR(20),
    PRIMARY KEY (tenant_id, type)
);

-- 商品分类
CREATE TABLE IF NOT EXISTS product_category (
    category_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id      VARCHAR(50) NOT NULL,
    biz_code       VARCHAR(50) NOT NULL,
    del_flag       INTEGER DEFAULT 0,
    category_name  VARCHAR(100) NOT NULL,
    sort           INTEGER DEFAULT 0,
    status         INTEGER DEFAULT 1,
    create_time    VARCHAR(20),
    UNIQUE(tenant_id, biz_code)
);

-- 商品单位
CREATE TABLE IF NOT EXISTS product_unit (
    unit_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id   VARCHAR(50) NOT NULL,
    biz_code    VARCHAR(50) NOT NULL,
    del_flag    INTEGER DEFAULT 0,
    unit_name   VARCHAR(30) NOT NULL,
    unit_en     VARCHAR(20) NOT NULL,
    sort        INTEGER DEFAULT 0,
    UNIQUE(tenant_id, biz_code)
);

-- 商品SPU
CREATE TABLE IF NOT EXISTS product_spu (
    spu_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id    VARCHAR(50) NOT NULL,
    biz_code     VARCHAR(50) NOT NULL,
    del_flag     INTEGER DEFAULT 0,
    category_id  INTEGER NOT NULL,
    spu_name     VARCHAR(200) NOT NULL,
    brand        VARCHAR(50),
    remark       VARCHAR(500),
    status       INTEGER DEFAULT 1,
    create_time  VARCHAR(20),
    UNIQUE(tenant_id, biz_code)
);

-- 商品SKU
CREATE TABLE IF NOT EXISTS product_sku (
    sku_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id       VARCHAR(50) NOT NULL,
    biz_code        VARCHAR(50) NOT NULL,
    del_flag        INTEGER DEFAULT 0,
    spu_id          INTEGER NOT NULL,
    bar_code        VARCHAR(50) NOT NULL,
    sku_name        VARCHAR(200) NOT NULL,
    stock_unit_id   INTEGER NOT NULL,
    cost_price      REAL NOT NULL,
    sale_price      REAL NOT NULL,
    stock           INTEGER DEFAULT 0,
    stock_warning   INTEGER DEFAULT 5,
    status          INTEGER DEFAULT 1,
    create_time     VARCHAR(20),
    update_time     VARCHAR(20),
    UNIQUE(tenant_id, biz_code),
    UNIQUE(tenant_id, bar_code)
);

-- SKU销售单位
CREATE TABLE IF NOT EXISTS product_sku_sale_unit (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER DEFAULT NULL,
    sku_id INTEGER NOT NULL,
    sku_no VARCHAR(64),
    barcode VARCHAR(64) DEFAULT NULL,
    goods_name VARCHAR(128) DEFAULT NULL,
    category_id INTEGER DEFAULT NULL,
    brand_id INTEGER DEFAULT NULL,
    sale_unit_id INTEGER DEFAULT NULL,
    sale_unit_name VARCHAR(128) DEFAULT NULL,
    sale_unit_symbol VARCHAR(32) DEFAULT NULL,
    conversion_rate REAL DEFAULT NULL,
    package_spec VARCHAR(256) DEFAULT NULL,
    sale_price REAL DEFAULT NULL,
    tax_rate REAL DEFAULT 0.16,
    is_tax_inclusive INTEGER DEFAULT 0,
    sale_price_usd REAL DEFAULT 0.0,
    batch_managed INTEGER DEFAULT 0,
    is_default INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    del_flag INTEGER DEFAULT 0,
    create_by INTEGER DEFAULT NULL,
    create_time VARCHAR(64),
    update_by INTEGER DEFAULT NULL,
    update_time VARCHAR(64)
);

-- 会员等级
CREATE TABLE IF NOT EXISTS member_level (
    level_id     INTEGER PRIMARY KEY,
    tenant_id    VARCHAR(50) NOT NULL,
    biz_code     VARCHAR(50) NOT NULL,
    del_flag     INTEGER DEFAULT 0,
    level_name   VARCHAR(50) NOT NULL,
    discount     REAL DEFAULT 1.00,
    need_point   INTEGER DEFAULT 0,
    UNIQUE(tenant_id, biz_code)
);

-- 会员
CREATE TABLE IF NOT EXISTS member (
    member_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id   VARCHAR(50) NOT NULL,
    biz_code    VARCHAR(50) NOT NULL,
    del_flag    INTEGER DEFAULT 0,
    phone       VARCHAR(20) NOT NULL,
    name        VARCHAR(50),
    password    VARCHAR(50) DEFAULT '123456',
    balance     REAL DEFAULT 0.00,
    point       INTEGER DEFAULT 0,
    level       INTEGER DEFAULT 1,
    status      INTEGER DEFAULT 1,
    create_time VARCHAR(20),
    update_time VARCHAR(20),
    UNIQUE(tenant_id, biz_code),
    UNIQUE(tenant_id, phone)
);

-- 会员价格
CREATE TABLE IF NOT EXISTS member_price (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id     VARCHAR(50) NOT NULL,
    biz_code      VARCHAR(50) NOT NULL,
    del_flag      INTEGER DEFAULT 0,
    sku_id        INTEGER NOT NULL,
    member_level  INTEGER DEFAULT 1,
    member_price  REAL NOT NULL,
    start_time    VARCHAR(20),
    end_time      VARCHAR(20),
    status        INTEGER DEFAULT 1,
    UNIQUE(tenant_id, biz_code)
);

-- 操作员
CREATE TABLE IF NOT EXISTS operator (
    operator_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id    VARCHAR(50) NOT NULL,
    biz_code     VARCHAR(50) NOT NULL,
    shop_code    VARCHAR(50) NOT NULL,
    username     VARCHAR(50) NOT NULL,
    del_flag     INTEGER DEFAULT 0,
    password     VARCHAR(50) NOT NULL,
    real_name    VARCHAR(50),
    role         INTEGER DEFAULT 1,
    status       INTEGER DEFAULT 1,
    create_time  VARCHAR(20),
    UNIQUE(tenant_id, biz_code),
    UNIQUE(tenant_id, shop_code, username)
);

-- 支付方式
CREATE TABLE IF NOT EXISTS pay_type (
    pay_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id VARCHAR(50) NOT NULL,
    biz_code  VARCHAR(50) NOT NULL,
    del_flag  INTEGER DEFAULT 0,
    pay_name  VARCHAR(50) NOT NULL,
    status    INTEGER DEFAULT 1,
    UNIQUE(tenant_id, biz_code)
);

-- 系统配置
CREATE TABLE IF NOT EXISTS sys_config (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    pos_sn        VARCHAR(100) NOT NULL,
    tenant_id     VARCHAR(50) NOT NULL,
    tenant_name   VARCHAR(100),
    shop_code     VARCHAR(50) NOT NULL,
    shop_name     VARCHAR(100),
    pos_id        VARCHAR(50) NOT NULL,
    pos_name      VARCHAR(100),
    update_time   VARCHAR(20),
    only_one      INTEGER NOT NULL DEFAULT 1 UNIQUE
);

-- 订单主表
CREATE TABLE IF NOT EXISTS order_main (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no         VARCHAR(50) NOT NULL,
    tenant_id        VARCHAR(50) NOT NULL,
    shop_code        VARCHAR(50) NOT NULL,
    pos_id           VARCHAR(30) NOT NULL,
    biz_type         INTEGER NOT NULL,
    total_amount     REAL NOT NULL,
    real_amount      REAL NOT NULL DEFAULT 0.00,
    discount_amount  REAL DEFAULT 0.00,
    member_id        INTEGER,
    operator_id      INTEGER NOT NULL,
    pay_status       INTEGER DEFAULT 0,
    create_by        VARCHAR(50),
    create_time      VARCHAR(20),
    update_by        VARCHAR(50),
    update_time      VARCHAR(20)
);

-- 订单明细
CREATE TABLE IF NOT EXISTS order_item (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id       VARCHAR(50) NOT NULL,
    shop_code       VARCHAR(50) NOT NULL,
    pos_id          VARCHAR(30) NOT NULL,
    order_no        VARCHAR(50) NOT NULL,
    biz_type        INTEGER NOT NULL,
    sku_id          INTEGER NOT NULL,
    sku_code        VARCHAR(50) NOT NULL,
    bar_code        VARCHAR(50) NOT NULL,
    product_name    VARCHAR(200) NOT NULL,
    sale_unit_name  VARCHAR(30) NOT NULL,
    sale_quantity   REAL NOT NULL,
    unit_price      REAL NOT NULL,
    member_price    REAL DEFAULT 0.00,
    subtotal        REAL NOT NULL,
    create_by       VARCHAR(50),
    create_time     VARCHAR(20),
    update_by       VARCHAR(50),
    update_time     VARCHAR(20)
);

-- 订单支付
CREATE TABLE IF NOT EXISTS order_pay (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id   VARCHAR(50) NOT NULL,
    shop_code   VARCHAR(50) NOT NULL,
    pos_id      VARCHAR(30) NOT NULL,
    order_no    VARCHAR(50) NOT NULL,
    biz_type    INTEGER NOT NULL,
    pay_id      INTEGER NOT NULL,
    pay_amount  REAL NOT NULL,
    pay_time    VARCHAR(20),
    create_by   VARCHAR(50),
    create_time VARCHAR(20),
    update_by   VARCHAR(50),
    update_time VARCHAR(20)
);

-- 退款单
CREATE TABLE IF NOT EXISTS refund_order (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    refund_no        VARCHAR(50) NOT NULL,
    tenant_id        VARCHAR(50) NOT NULL,
    shop_code        VARCHAR(50) NOT NULL,
    pos_id           VARCHAR(30) NOT NULL,
    biz_type         INTEGER NOT NULL DEFAULT 3,
    source_order_no  VARCHAR(50) NOT NULL,
    operator_id      INTEGER NOT NULL,
    total_refund     REAL NOT NULL,
    net_refund       REAL DEFAULT 0.00,
    tax_refund       REAL DEFAULT 0.00,
    currency         VARCHAR(3) DEFAULT 'ZMW',
    exchange_rate    REAL DEFAULT 1.0,
    original_refund  REAL DEFAULT 0.00,
    refund_time      VARCHAR(20) NOT NULL,
    refund_type      INTEGER DEFAULT 1,
    remark           VARCHAR(500),
    create_by        VARCHAR(50),
    create_time      VARCHAR(20),
    update_by        VARCHAR(50),
    update_time      VARCHAR(20)
);

-- 退款明细
CREATE TABLE IF NOT EXISTS refund_order_item (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id         VARCHAR(50) NOT NULL,
    shop_code         VARCHAR(50) NOT NULL,
    pos_id            VARCHAR(30) NOT NULL,
    refund_no         VARCHAR(50) NOT NULL,
    biz_type          INTEGER NOT NULL DEFAULT 3,
    sku_id            INTEGER NOT NULL,
    sku_code          VARCHAR(50) NOT NULL,
    bar_code          VARCHAR(50) NOT NULL,
    product_name      VARCHAR(200) NOT NULL,
    sale_unit_name    VARCHAR(30) NOT NULL,
    refund_quantity   REAL NOT NULL,
    refund_price      REAL NOT NULL,
    subtotal          REAL NOT NULL,
    tax_rate          REAL DEFAULT 0.16,
    is_tax_inclusive  INTEGER DEFAULT 0,
    net_amount        REAL DEFAULT 0.00,
    tax_amount        REAL DEFAULT 0.00,
    batch_no          VARCHAR(64),
    currency          VARCHAR(3) DEFAULT 'ZMW',
    create_by         VARCHAR(50),
    create_time       VARCHAR(20),
    update_by         VARCHAR(50),
    update_time       VARCHAR(20)
);

-- 退款支付
CREATE TABLE IF NOT EXISTS refund_pay (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id       VARCHAR(50) NOT NULL,
    shop_code       VARCHAR(50) NOT NULL,
    pos_id          VARCHAR(30) NOT NULL,
    refund_no       VARCHAR(50) NOT NULL,
    biz_type        INTEGER NOT NULL DEFAULT 7,
    pay_id          INTEGER NOT NULL,
    pay_name        VARCHAR(50) NOT NULL,
    refund_amount   REAL NOT NULL,
    currency        VARCHAR(3) DEFAULT 'ZMW',
    refund_time     VARCHAR(20),
    create_by       VARCHAR(50),
    create_time     VARCHAR(20),
    update_by       VARCHAR(50),
    update_time     VARCHAR(20)
);

-- 挂单
CREATE TABLE IF NOT EXISTS order_pending (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    pending_no       VARCHAR(50) NOT NULL,
    tenant_id        VARCHAR(50) NOT NULL,
    shop_code        VARCHAR(50) NOT NULL,
    pos_id           VARCHAR(30) NOT NULL,
    biz_type         INTEGER NOT NULL DEFAULT 4,
    operator_id      INTEGER NOT NULL,
    pending_name     VARCHAR(100),
    total_amount     REAL NOT NULL DEFAULT 0.00,
    create_by        VARCHAR(50),
    create_time      VARCHAR(20),
    update_by        VARCHAR(50),
    update_time      VARCHAR(20)
);

-- 挂单明细
CREATE TABLE IF NOT EXISTS order_pending_item (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id       VARCHAR(50) NOT NULL,
    shop_code       VARCHAR(50) NOT NULL,
    pos_id          VARCHAR(30) NOT NULL,
    pending_no      VARCHAR(50) NOT NULL,
    biz_type        INTEGER NOT NULL DEFAULT 4,
    sku_id          INTEGER NOT NULL,
    sku_code        VARCHAR(50) NOT NULL,
    bar_code        VARCHAR(50) NOT NULL,
    product_name    VARCHAR(200) NOT NULL,
    sale_unit_name  VARCHAR(30) NOT NULL,
    sale_quantity   REAL NOT NULL,
    unit_price      REAL NOT NULL,
    member_price    REAL DEFAULT 0.00,
    subtotal        REAL NOT NULL,
    create_by       VARCHAR(50),
    create_time     VARCHAR(20),
    update_by       VARCHAR(50),
    update_time     VARCHAR(20)
);

-- 交班结算
CREATE TABLE IF NOT EXISTS cashier_settlement (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    settle_no        VARCHAR(50) NOT NULL,
    tenant_id        VARCHAR(50) NOT NULL,
    shop_code        VARCHAR(50) NOT NULL,
    pos_id           VARCHAR(30) NOT NULL,
    biz_type         INTEGER NOT NULL DEFAULT 5,
    operator_id      INTEGER NOT NULL,
    username         VARCHAR(50) NOT NULL,
    real_name        VARCHAR(50) NOT NULL,
    begin_time       VARCHAR(20) NOT NULL,
    end_time         VARCHAR(20) NOT NULL,
    total_order      INTEGER DEFAULT 0,
    total_amount     REAL DEFAULT 0.00,
    total_real       REAL DEFAULT 0.00,
    total_discount   REAL DEFAULT 0.00,
    cash_amount      REAL DEFAULT 0.00,
    wechat_amount    REAL DEFAULT 0.00,
    alipay_amount    REAL DEFAULT 0.00,
    member_amount    REAL DEFAULT 0.00,
    other_amount     REAL DEFAULT 0.00,
    status           INTEGER DEFAULT 0,
    create_by        VARCHAR(50),
    create_time      VARCHAR(20),
    update_by        VARCHAR(50),
    update_time      VARCHAR(20)
);

-- 班次记录
CREATE TABLE IF NOT EXISTS cashier_shift (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    shift_no         VARCHAR(50) NOT NULL,
    tenant_id        VARCHAR(50) NOT NULL,
    shop_code        VARCHAR(50) NOT NULL,
    pos_id           VARCHAR(30) NOT NULL,
    biz_type         INTEGER NOT NULL DEFAULT 6,
    operator_id      INTEGER NOT NULL,
    operator_name    VARCHAR(50) NOT NULL,
    begin_time       VARCHAR(20) NOT NULL,
    end_time         VARCHAR(20) NOT NULL,
    cash_begin       REAL DEFAULT 0.00,
    cash_end         REAL DEFAULT 0.00,
    cash_reality     REAL DEFAULT 0.00,
    cash_diff        REAL DEFAULT 0.00,
    total_order      INTEGER DEFAULT 0,
    total_amount     REAL DEFAULT 0.00,
    total_real       REAL DEFAULT 0.00,
    total_discount   REAL DEFAULT 0.00,
    cash_amount      REAL DEFAULT 0.00,
    wechat_amount    REAL DEFAULT 0.00,
    alipay_amount    REAL DEFAULT 0.00,
    member_amount    REAL DEFAULT 0.00,
    other_amount     REAL DEFAULT 0.00,
    status           INTEGER DEFAULT 0,
    remark           VARCHAR(500),
    create_by        VARCHAR(50),
    create_time      VARCHAR(20),
    update_by        VARCHAR(50),
    update_time      VARCHAR(20),
    UNIQUE(tenant_id, shift_no)
);

-- 班次支付方式明细
CREATE TABLE IF NOT EXISTS cashier_shift_pay (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id        VARCHAR(50) NOT NULL,
    shop_code        VARCHAR(50) NOT NULL,
    pos_id           VARCHAR(30) NOT NULL,
    shift_no         VARCHAR(50) NOT NULL,
    pay_id           INTEGER NOT NULL,
    pay_name         VARCHAR(50) NOT NULL,
    pay_amount       REAL DEFAULT 0.00,
    create_by        VARCHAR(50),
    create_time      VARCHAR(20)
);
