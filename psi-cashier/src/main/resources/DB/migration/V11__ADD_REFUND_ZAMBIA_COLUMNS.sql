-- 为退款主表增加赞比亚 VAT/币种字段
ALTER TABLE refund_order ADD COLUMN net_refund REAL DEFAULT 0.00;
ALTER TABLE refund_order ADD COLUMN tax_refund REAL DEFAULT 0.00;
ALTER TABLE refund_order ADD COLUMN currency VARCHAR(3) DEFAULT 'ZMW';
ALTER TABLE refund_order ADD COLUMN exchange_rate REAL DEFAULT 1.0;
ALTER TABLE refund_order ADD COLUMN original_refund REAL DEFAULT 0.00;

-- 为退款明细增加 VAT/批次/币种字段
ALTER TABLE refund_order_item ADD COLUMN tax_rate REAL DEFAULT 0.16;
ALTER TABLE refund_order_item ADD COLUMN is_tax_inclusive INTEGER DEFAULT 0;
ALTER TABLE refund_order_item ADD COLUMN net_amount REAL DEFAULT 0.00;
ALTER TABLE refund_order_item ADD COLUMN tax_amount REAL DEFAULT 0.00;
ALTER TABLE refund_order_item ADD COLUMN batch_no VARCHAR(64);
ALTER TABLE refund_order_item ADD COLUMN currency VARCHAR(3) DEFAULT 'ZMW';

-- 为退款支付增加币种字段
ALTER TABLE refund_pay ADD COLUMN currency VARCHAR(3) DEFAULT 'ZMW';
