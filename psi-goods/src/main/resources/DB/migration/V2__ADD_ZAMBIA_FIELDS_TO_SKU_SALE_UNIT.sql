-- 为 SKU 销售单位表增加赞比亚场景必要字段
ALTER TABLE goods_sku_sale_unit ADD COLUMN tax_rate DECIMAL(5,4) DEFAULT 0.1600 COMMENT 'VAT税率（如0.1600表示16%）';
ALTER TABLE goods_sku_sale_unit ADD COLUMN is_tax_inclusive TINYINT DEFAULT 0 COMMENT '标价是否含税(0:否 1:是)';
ALTER TABLE goods_sku_sale_unit ADD COLUMN sale_price_usd DECIMAL(12,4) DEFAULT 0.0000 COMMENT 'USD销售价';
ALTER TABLE goods_sku_sale_unit ADD COLUMN batch_managed TINYINT DEFAULT 0 COMMENT '是否管理批次/效期(0:否 1:是)';
