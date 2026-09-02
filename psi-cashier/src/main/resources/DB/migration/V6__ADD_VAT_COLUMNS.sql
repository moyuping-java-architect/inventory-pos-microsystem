-- 为收银订单增加 VAT 税额拆分字段

ALTER TABLE order_main ADD COLUMN tax_amount REAL DEFAULT 0.00;
ALTER TABLE order_main ADD COLUMN net_amount REAL DEFAULT 0.00;

ALTER TABLE order_item ADD COLUMN tax_rate REAL DEFAULT 0.16;
ALTER TABLE order_item ADD COLUMN tax_amount REAL DEFAULT 0.00;
ALTER TABLE order_item ADD COLUMN net_amount REAL DEFAULT 0.00;
