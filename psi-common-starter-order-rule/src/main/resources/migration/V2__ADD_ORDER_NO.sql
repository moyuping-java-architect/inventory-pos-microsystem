-- 为 doc_main_draft 表添加 order_no 字段
-- 用于存储关联订单号（如采购入库关联的采购订单号）

ALTER TABLE doc_main_draft
ADD COLUMN IF NOT EXISTS order_no VARCHAR(64) DEFAULT NULL COMMENT '关联订单号（用于入库/出库等关联上游单据）' AFTER warehouse_name;

-- 为 purchase_in_main 表添加 order_no 字段
-- 用于存储关联的采购订单号

ALTER TABLE purchase_in_main
ADD COLUMN IF NOT EXISTS order_no VARCHAR(64) DEFAULT NULL COMMENT '关联采购订单号' AFTER warehouse_name;

-- 添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_order_no ON doc_main_draft(order_no);
CREATE INDEX IF NOT EXISTS idx_order_no ON purchase_in_main(order_no);