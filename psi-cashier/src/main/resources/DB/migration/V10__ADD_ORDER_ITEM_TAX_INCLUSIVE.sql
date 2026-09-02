-- 为收银订单明细增加含税价标识
ALTER TABLE order_item ADD COLUMN is_tax_inclusive INTEGER DEFAULT 0;
