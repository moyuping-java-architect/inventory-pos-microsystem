-- 为收银机本地 SKU 销售单位表增加赞比亚场景字段
ALTER TABLE product_sku_sale_unit ADD COLUMN tax_rate REAL DEFAULT 0.16;
ALTER TABLE product_sku_sale_unit ADD COLUMN is_tax_inclusive INTEGER DEFAULT 0;
ALTER TABLE product_sku_sale_unit ADD COLUMN sale_price_usd REAL DEFAULT 0.0;
ALTER TABLE product_sku_sale_unit ADD COLUMN batch_managed INTEGER DEFAULT 0;
