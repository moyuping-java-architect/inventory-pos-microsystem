-- 创建汇率表
CREATE TABLE IF NOT EXISTS exchange_rate (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    rate REAL NOT NULL,
    effective_date DATE NOT NULL,
    create_time VARCHAR(20),
    update_time VARCHAR(20)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_from_to_date ON exchange_rate(from_currency, to_currency, effective_date);
