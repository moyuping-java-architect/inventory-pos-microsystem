-- SQLite兼容版本：添加data_uuid字段并创建唯一索引
-- data_uuid由业务层使用雪花算法生成，不在数据库层生成

-- 1. product_category - 重命名biz_code为data_uuid
ALTER TABLE product_category ADD COLUMN data_uuid VARCHAR(64);
UPDATE product_category SET data_uuid = biz_code;
ALTER TABLE product_category DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_product_category_data_uuid ON product_category(data_uuid);

-- 2. product_unit - 重命名biz_code为data_uuid
ALTER TABLE product_unit ADD COLUMN data_uuid VARCHAR(64);
UPDATE product_unit SET data_uuid = biz_code;
ALTER TABLE product_unit DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_product_unit_data_uuid ON product_unit(data_uuid);

-- 3. product_spu - 重命名biz_code为data_uuid
ALTER TABLE product_spu ADD COLUMN data_uuid VARCHAR(64);
UPDATE product_spu SET data_uuid = biz_code;
ALTER TABLE product_spu DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_product_spu_data_uuid ON product_spu(data_uuid);

-- 4. product_sku - 重命名biz_code为data_uuid
ALTER TABLE product_sku ADD COLUMN data_uuid VARCHAR(64);
UPDATE product_sku SET data_uuid = biz_code;
ALTER TABLE product_sku DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_product_sku_data_uuid ON product_sku(data_uuid);

-- 5. member_level - 重命名biz_code为data_uuid
ALTER TABLE member_level ADD COLUMN data_uuid VARCHAR(64);
UPDATE member_level SET data_uuid = biz_code;
ALTER TABLE member_level DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_member_level_data_uuid ON member_level(data_uuid);

-- 6. member - 重命名biz_code为data_uuid
ALTER TABLE member ADD COLUMN data_uuid VARCHAR(64);
UPDATE member SET data_uuid = biz_code;
ALTER TABLE member DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_member_data_uuid ON member(data_uuid);

-- 7. member_price - 重命名biz_code为data_uuid
ALTER TABLE member_price ADD COLUMN data_uuid VARCHAR(64);
UPDATE member_price SET data_uuid = biz_code;
ALTER TABLE member_price DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_member_price_data_uuid ON member_price(data_uuid);

-- 8. operator - 重命名biz_code为data_uuid
ALTER TABLE operator ADD COLUMN data_uuid VARCHAR(64);
UPDATE operator SET data_uuid = biz_code;
ALTER TABLE operator DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_operator_data_uuid ON operator(data_uuid);

-- 9. pay_type - 重命名biz_code为data_uuid
ALTER TABLE pay_type ADD COLUMN data_uuid VARCHAR(64);
UPDATE pay_type SET data_uuid = biz_code;
ALTER TABLE pay_type DROP COLUMN biz_code;
CREATE UNIQUE INDEX idx_pay_type_data_uuid ON pay_type(data_uuid);

-- 10. product_sku_sale_unit - 添加data_uuid字段
ALTER TABLE product_sku_sale_unit ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_product_sku_sale_unit_data_uuid ON product_sku_sale_unit(data_uuid);

-- 11. sys_config - 添加data_uuid字段
ALTER TABLE sys_config ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_sys_config_data_uuid ON sys_config(data_uuid);

-- 12. order_main - 添加data_uuid字段
ALTER TABLE order_main ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_order_main_data_uuid ON order_main(data_uuid);

-- 13. order_item - 添加data_uuid字段
ALTER TABLE order_item ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_order_item_data_uuid ON order_item(data_uuid);

-- 14. order_pay - 添加data_uuid字段
ALTER TABLE order_pay ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_order_pay_data_uuid ON order_pay(data_uuid);

-- 15. refund_order - 添加data_uuid字段
ALTER TABLE refund_order ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_refund_order_data_uuid ON refund_order(data_uuid);

-- 16. refund_order_item - 添加data_uuid字段
ALTER TABLE refund_order_item ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_refund_order_item_data_uuid ON refund_order_item(data_uuid);

-- 17. refund_pay - 添加data_uuid字段
ALTER TABLE refund_pay ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_refund_pay_data_uuid ON refund_pay(data_uuid);

-- 18. order_pending - 添加data_uuid字段
ALTER TABLE order_pending ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_order_pending_data_uuid ON order_pending(data_uuid);

-- 19. order_pending_item - 添加data_uuid字段
ALTER TABLE order_pending_item ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_order_pending_item_data_uuid ON order_pending_item(data_uuid);

-- 20. cashier_settlement - 添加data_uuid字段
ALTER TABLE cashier_settlement ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_cashier_settlement_data_uuid ON cashier_settlement(data_uuid);

-- 21. refund_item - 添加data_uuid字段
ALTER TABLE refund_item ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_refund_item_data_uuid ON refund_item(data_uuid);

-- 22. cashier_shift - 添加data_uuid字段
ALTER TABLE cashier_shift ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_cashier_shift_data_uuid ON cashier_shift(data_uuid);

-- 23. cashier_shift_pay - 添加data_uuid字段
ALTER TABLE cashier_shift_pay ADD COLUMN data_uuid VARCHAR(64);
CREATE UNIQUE INDEX idx_cashier_shift_pay_data_uuid ON cashier_shift_pay(data_uuid);