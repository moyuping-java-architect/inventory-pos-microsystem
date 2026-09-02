-- 收银员班次结算主表
CREATE TABLE cashier_shift (
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

-- 班次支付方式明细表
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