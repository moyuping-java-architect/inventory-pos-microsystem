-- 为收银订单扩展多币种与 Mobile Money 字段

-- 主订单增加币种与汇率
ALTER TABLE order_main ADD COLUMN currency VARCHAR(3) DEFAULT 'ZMW';
ALTER TABLE order_main ADD COLUMN exchange_rate REAL DEFAULT 1.0;
ALTER TABLE order_main ADD COLUMN original_amount REAL DEFAULT 0.00;

-- 支付明细增加 Mobile Money 字段
ALTER TABLE order_pay ADD COLUMN mobile_provider VARCHAR(20);
ALTER TABLE order_pay ADD COLUMN mobile_phone VARCHAR(20);
ALTER TABLE order_pay ADD COLUMN mobile_transaction_no VARCHAR(64);
ALTER TABLE order_pay ADD COLUMN pay_type INT DEFAULT 0;
ALTER TABLE order_pay ADD COLUMN currency VARCHAR(3) DEFAULT 'ZMW';

-- 订单明细增加批次与币种
ALTER TABLE order_item ADD COLUMN batch_no VARCHAR(64);
ALTER TABLE order_item ADD COLUMN currency VARCHAR(3) DEFAULT 'ZMW';
