-- 采购订单明细增加含税价标识
ALTER TABLE purchase_order_item ADD COLUMN is_tax_inclusive TINYINT DEFAULT 0 COMMENT '标价是否含税(0:否 1:是)';
