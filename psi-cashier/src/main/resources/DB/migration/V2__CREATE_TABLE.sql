CREATE TABLE sys_seq (
    pos_id      VARCHAR(30) NOT NULL,
    seq_type    VARCHAR(20) NOT NULL,
    day         VARCHAR(8) NOT NULL,
    curr_no     INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (pos_id, seq_type, day)
);

CREATE TABLE sync_log (
    tenant_id      VARCHAR(50) NOT NULL,
    type       VARCHAR(10) NOT NULL PRIMARY KEY,
    last_download_time  VARCHAR(20)，
    PRIMARY KEY (tenant_id, type)
);

CREATE TABLE product_category (
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

CREATE TABLE product_unit (
    unit_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id   VARCHAR(50) NOT NULL,
    biz_code    VARCHAR(50) NOT NULL,
    del_flag    INTEGER DEFAULT 0,
    unit_name   VARCHAR(30) NOT NULL,
    unit_en     VARCHAR(20) NOT NULL,
    sort        INTEGER DEFAULT 0,
    UNIQUE(tenant_id, biz_code)
);

CREATE TABLE product_spu (
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

CREATE TABLE product_sku (
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

CREATE TABLE product_sku_sale_unit (
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

CREATE TABLE member_level (
    level_id     INTEGER PRIMARY KEY,
    tenant_id    VARCHAR(50) NOT NULL,
    biz_code     VARCHAR(50) NOT NULL,
    del_flag     INTEGER DEFAULT 0,
    level_name   VARCHAR(50) NOT NULL,
    discount     REAL DEFAULT 1.00,
    need_point   INTEGER DEFAULT 0,
    UNIQUE(tenant_id, biz_code)
);

CREATE TABLE member (
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

CREATE TABLE member_price (
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

CREATE TABLE operator (
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

CREATE TABLE pay_type (
    pay_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id VARCHAR(50) NOT NULL,
    biz_code  VARCHAR(50) NOT NULL,
    del_flag  INTEGER DEFAULT 0,
    pay_name  VARCHAR(50) NOT NULL,
    status    INTEGER DEFAULT 1,
    UNIQUE(tenant_id, biz_code)
);

CREATE TABLE sys_config (
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

CREATE TABLE order_main (
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

CREATE TABLE order_item (
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

CREATE TABLE order_pay (
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

CREATE TABLE refund_order (
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

CREATE TABLE refund_order_item (
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

CREATE TABLE refund_pay (
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

CREATE TABLE order_pending (
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

CREATE TABLE order_pending_item (
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

CREATE TABLE cashier_settlement (
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

CREATE TABLE IF NOT EXISTS refund_item (
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   return_uuid TEXT NOT NULL,                -- 关联退货单
   pay_type INTEGER NOT NULL,                -- 退款方式 1现金 2微信 3支付宝 4会员卡
   refund_amount REAL NOT NULL DEFAULT 0.0,  -- 单笔退款金额
   create_time TEXT NOT NULL,                -- 时间
   del_flag INTEGER NOT NULL DEFAULT 0,      -- 软删除
FOREIGN KEY (return_uuid) REFERENCES sale_return(return_uuid)
    );

-- 收银员班次记录表（下班结账记录）
CREATE TABLE cashier_shift (
                               id               INTEGER PRIMARY KEY AUTOINCREMENT,
                               shift_no         VARCHAR(50) NOT NULL,
                               tenant_id        VARCHAR(50) NOT NULL,
                               shop_code        VARCHAR(50) NOT NULL,
                               pos_id           VARCHAR(30) NOT NULL,
                               biz_type         INTEGER NOT NULL DEFAULT 6,

    -- 收银员信息
                               operator_id      INTEGER NOT NULL,
                               operator_name    VARCHAR(50) NOT NULL,

    -- 班次时间
                               begin_time       VARCHAR(20) NOT NULL,
                               end_time         VARCHAR(20) NOT NULL,

    -- 现金交接（最重要）
                               cash_begin       REAL DEFAULT 0.00,   -- 上班时的备用金
                               cash_end         REAL DEFAULT 0.00,   -- 系统计算的现金总额
                               cash_reality     REAL DEFAULT 0.00,   -- 实际清点金额
                               cash_diff        REAL DEFAULT 0.00,   -- 差异（end - reality）

    -- 本班次统计
                               total_order      INTEGER DEFAULT 0,    -- 订单总数
                               total_amount     REAL DEFAULT 0.00,   -- 商品原价总额
                               total_real       REAL DEFAULT 0.00,   -- 实际收款总额
                               total_discount   REAL DEFAULT 0.00,   -- 折扣总额

    -- 各支付方式金额
                               cash_amount      REAL DEFAULT 0.00,   -- 现金收款
                               wechat_amount    REAL DEFAULT 0.00,   -- 微信收款
                               alipay_amount    REAL DEFAULT 0.00,   -- 支付宝收款
                               member_amount    REAL DEFAULT 0.00,   -- 会员卡收款
                               other_amount     REAL DEFAULT 0.00,   -- 其他收款

    -- 状态：0-待确认 1-已完成 2-有差异待处理
                               status           INTEGER DEFAULT 0,

    -- 备注（可记录差异原因等）
                               remark           VARCHAR(500),

                               create_by        VARCHAR(50),
                               create_time      VARCHAR(20),
                               update_by        VARCHAR(50),
                               update_time      VARCHAR(20),

                               UNIQUE(tenant_id, shift_no)
);

-- 班次支付方式明细（可选，用于记录更多支付方式）
CREATE TABLE cashier_shift_pay (
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