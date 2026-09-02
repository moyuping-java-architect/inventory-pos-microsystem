-- 为 doc_item_draft 表添加 barcode 字段
ALTER TABLE doc_item_draft ADD COLUMN barcode VARCHAR(64) DEFAULT NULL COMMENT '条码' AFTER goods_code;