-- 商品 SKU 增加 USD 定价与含税标识字段
ALTER TABLE goods_sku ADD COLUMN sale_price_usd DECIMAL(12,4) DEFAULT 0.0000 COMMENT 'USD 销售价';
ALTER TABLE goods_sku ADD COLUMN cost_price_usd DECIMAL(12,4) DEFAULT 0.0000 COMMENT 'USD 成本价';
ALTER TABLE goods_sku ADD COLUMN is_tax_inclusive TINYINT DEFAULT 0 COMMENT '标价是否含税(0:否 1:是)';
