-- ============================================================
-- fix_schema.sql
-- Database schema fix script for psi-modular project
-- Contents:
--   1. Create missing stock formal tables (main + item pairs)
--   2. Add missing order_status column to existing sale/purchase tables
-- All comments are in English to avoid encoding issues.
-- All CREATE TABLE statements use IF NOT EXISTS.
-- For ALTER TABLE ADD COLUMN, a stored procedure is used because
-- MySQL 8.x does not support ADD COLUMN IF NOT EXISTS natively.
-- ============================================================

-- ============================================================
-- 1. Missing stock formal tables
-- ============================================================

-- -------------------- stock_loss_main --------------------
CREATE TABLE IF NOT EXISTS `stock_loss_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `loss_no` VARCHAR(64) NOT NULL COMMENT 'stock loss doc no',
    `doc_name` VARCHAR(200) NOT NULL COMMENT 'doc name (required, default: doc type + today date)',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT 'warehouse code',
    `warehouse_name` VARCHAR(128) DEFAULT NULL COMMENT 'warehouse name',
    `loss_date` VARCHAR(20) DEFAULT NULL COMMENT 'loss date',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'total loss amount',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'tax amount',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_loss_no` (`loss_no`),
    INDEX `idx_warehouse_code` (`warehouse_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock loss main';

-- -------------------- stock_loss_item --------------------
CREATE TABLE IF NOT EXISTS `stock_loss_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `loss_id` BIGINT NOT NULL COMMENT 'stock loss main id',
    `loss_no` VARCHAR(64) NOT NULL COMMENT 'stock loss doc no',
    `goods_code` VARCHAR(64) NOT NULL COMMENT 'goods code',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'sku code',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'sku name',
    `goods_name` VARCHAR(128) DEFAULT NULL COMMENT 'goods name',
    `goods_spec` VARCHAR(256) DEFAULT NULL COMMENT 'goods spec',
    `unit` VARCHAR(32) DEFAULT NULL COMMENT 'unit',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT 'conversion rate from sale unit to stock base unit',
    `loss_quantity` DECIMAL(18,4) NOT NULL COMMENT 'loss quantity',
    `unit_price` DECIMAL(18,4) DEFAULT NULL COMMENT 'unit price',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'amount',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'tax amount',
    `loss_reason` VARCHAR(500) DEFAULT NULL COMMENT 'loss reason',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_loss_id` (`loss_id`),
    INDEX `idx_goods_code` (`goods_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock loss item';

-- -------------------- stock_over_main --------------------
CREATE TABLE IF NOT EXISTS `stock_over_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `over_no` VARCHAR(64) NOT NULL COMMENT 'stock over doc no',
    `doc_name` VARCHAR(200) NOT NULL COMMENT 'doc name (required, default: doc type + today date)',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT 'warehouse code',
    `warehouse_name` VARCHAR(128) DEFAULT NULL COMMENT 'warehouse name',
    `over_date` VARCHAR(20) DEFAULT NULL COMMENT 'over date',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'total over amount',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'tax amount',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_over_no` (`over_no`),
    INDEX `idx_warehouse_code` (`warehouse_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock over main';

-- -------------------- stock_over_item --------------------
CREATE TABLE IF NOT EXISTS `stock_over_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `over_id` BIGINT NOT NULL COMMENT 'stock over main id',
    `over_no` VARCHAR(64) NOT NULL COMMENT 'stock over doc no',
    `goods_code` VARCHAR(64) NOT NULL COMMENT 'goods code',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'sku code',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'sku name',
    `goods_name` VARCHAR(128) DEFAULT NULL COMMENT 'goods name',
    `goods_spec` VARCHAR(256) DEFAULT NULL COMMENT 'goods spec',
    `unit` VARCHAR(32) DEFAULT NULL COMMENT 'unit',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT 'conversion rate from sale unit to stock base unit',
    `over_quantity` DECIMAL(18,4) NOT NULL COMMENT 'over quantity',
    `unit_price` DECIMAL(18,4) DEFAULT NULL COMMENT 'unit price',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'amount',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'tax amount',
    `over_reason` VARCHAR(500) DEFAULT NULL COMMENT 'over reason',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_over_id` (`over_id`),
    INDEX `idx_goods_code` (`goods_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock over item';

-- -------------------- stock_check_main --------------------
CREATE TABLE IF NOT EXISTS `stock_check_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `check_no` VARCHAR(64) NOT NULL COMMENT 'stock check doc no',
    `doc_name` VARCHAR(200) NOT NULL COMMENT 'doc name (required, default: doc type + today date)',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT 'warehouse code',
    `warehouse_name` VARCHAR(128) DEFAULT NULL COMMENT 'warehouse name',
    `check_date` VARCHAR(20) DEFAULT NULL COMMENT 'check date',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'book total amount',
    `diff_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'diff amount',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_check_no` (`check_no`),
    INDEX `idx_warehouse_code` (`warehouse_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock check main';

-- -------------------- stock_check_item --------------------
CREATE TABLE IF NOT EXISTS `stock_check_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `check_id` BIGINT NOT NULL COMMENT 'stock check main id',
    `check_no` VARCHAR(64) NOT NULL COMMENT 'stock check doc no',
    `goods_code` VARCHAR(64) NOT NULL COMMENT 'goods code',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'sku code',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'sku name',
    `goods_name` VARCHAR(128) DEFAULT NULL COMMENT 'goods name',
    `goods_spec` VARCHAR(256) DEFAULT NULL COMMENT 'goods spec',
    `unit` VARCHAR(32) DEFAULT NULL COMMENT 'unit',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT 'conversion rate from sale unit to stock base unit',
    `book_quantity` DECIMAL(18,4) NOT NULL COMMENT 'book quantity',
    `actual_quantity` DECIMAL(18,4) NOT NULL COMMENT 'actual quantity',
    `diff_quantity` DECIMAL(18,4) DEFAULT NULL COMMENT 'diff quantity',
    `unit_price` DECIMAL(18,4) DEFAULT NULL COMMENT 'unit price',
    `book_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'book amount',
    `actual_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'actual amount',
    `diff_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'diff amount',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_check_id` (`check_id`),
    INDEX `idx_goods_code` (`goods_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock check item';

-- -------------------- stock_transfer_main --------------------
CREATE TABLE IF NOT EXISTS `stock_transfer_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `transfer_no` VARCHAR(64) NOT NULL COMMENT 'stock transfer doc no',
    `doc_name` VARCHAR(200) NOT NULL COMMENT 'doc name (required, default: doc type + today date)',
    `from_warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT 'from warehouse code',
    `from_warehouse_name` VARCHAR(128) DEFAULT NULL COMMENT 'from warehouse name',
    `to_warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT 'to warehouse code',
    `to_warehouse_name` VARCHAR(128) DEFAULT NULL COMMENT 'to warehouse name',
    `transfer_date` VARCHAR(20) DEFAULT NULL COMMENT 'transfer date',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'total transfer amount',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'tax amount',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_transfer_no` (`transfer_no`),
    INDEX `idx_from_warehouse_code` (`from_warehouse_code`),
    INDEX `idx_to_warehouse_code` (`to_warehouse_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock transfer main';

-- -------------------- stock_transfer_item --------------------
CREATE TABLE IF NOT EXISTS `stock_transfer_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `transfer_id` BIGINT NOT NULL COMMENT 'stock transfer main id',
    `transfer_no` VARCHAR(64) NOT NULL COMMENT 'stock transfer doc no',
    `goods_code` VARCHAR(64) NOT NULL COMMENT 'goods code',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'sku code',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'sku name',
    `goods_name` VARCHAR(128) DEFAULT NULL COMMENT 'goods name',
    `goods_spec` VARCHAR(256) DEFAULT NULL COMMENT 'goods spec',
    `unit` VARCHAR(32) DEFAULT NULL COMMENT 'unit',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT 'conversion rate from sale unit to stock base unit',
    `transfer_quantity` DECIMAL(18,4) NOT NULL COMMENT 'transfer quantity',
    `unit_price` DECIMAL(18,4) DEFAULT NULL COMMENT 'unit price',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'amount',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'tax amount',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_transfer_id` (`transfer_id`),
    INDEX `idx_goods_code` (`goods_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock transfer item';

-- -------------------- stock_inventory_init_main --------------------
CREATE TABLE IF NOT EXISTS `stock_inventory_init_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `init_no` VARCHAR(64) NOT NULL COMMENT 'stock inventory init doc no',
    `doc_name` VARCHAR(200) NOT NULL COMMENT 'doc name (required, default: doc type + today date)',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT 'warehouse code',
    `warehouse_name` VARCHAR(128) DEFAULT NULL COMMENT 'warehouse name',
    `init_date` VARCHAR(20) DEFAULT NULL COMMENT 'init date',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'total init amount',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    `order_status` TINYINT DEFAULT 1 COMMENT 'order status (1-pending audit 2-audit passed 3-completed 4-canceled)',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_init_no` (`init_no`),
    INDEX `idx_warehouse_code` (`warehouse_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock inventory init main';

-- -------------------- stock_inventory_init_item --------------------
CREATE TABLE IF NOT EXISTS `stock_inventory_init_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'primary key id',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT 'data unique id (snowflake), used for distributed data sync',
    `tenant_id` BIGINT DEFAULT NULL COMMENT 'tenant id',
    `init_id` BIGINT NOT NULL COMMENT 'stock inventory init main id',
    `init_no` VARCHAR(64) NOT NULL COMMENT 'stock inventory init doc no',
    `goods_code` VARCHAR(64) NOT NULL COMMENT 'goods code',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'sku code',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'sku name',
    `goods_name` VARCHAR(128) DEFAULT NULL COMMENT 'goods name',
    `goods_spec` VARCHAR(256) DEFAULT NULL COMMENT 'goods spec',
    `unit` VARCHAR(32) DEFAULT NULL COMMENT 'unit',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT 'conversion rate from sale unit to stock base unit',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT 'init quantity',
    `unit_price` DECIMAL(18,4) DEFAULT NULL COMMENT 'unit price',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT 'amount',
    `status` TINYINT DEFAULT 0 COMMENT 'status (0:enabled 1:disabled)',
    `del_flag` TINYINT DEFAULT 0 COMMENT 'delete flag (0:not deleted 1:deleted)',
    `create_by` BIGINT DEFAULT NULL COMMENT 'create by user id',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_by` BIGINT DEFAULT NULL COMMENT 'update by user id',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_init_id` (`init_id`),
    INDEX `idx_goods_code` (`goods_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='stock inventory init item';

-- ============================================================
-- 2. Add missing order_status column to existing tables
-- MySQL 8.x does NOT support `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`.
-- We use a stored procedure that queries information_schema.columns
-- and only executes the ALTER when the column is missing.
-- The procedure is created, called for each table, then dropped.
-- ============================================================

DROP PROCEDURE IF EXISTS `psi_add_column_if_missing`;
DELIMITER $$
CREATE PROCEDURE `psi_add_column_if_missing`(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_def TEXT
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO col_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name;

    IF col_exists = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_column_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- sale_out_main: missing order_status (1-pending audit 2-audit passed 3-completed 4-canceled)
CALL psi_add_column_if_missing(
    'sale_out_main',
    'order_status',
    'TINYINT DEFAULT 1 COMMENT ''order status (1-pending audit 2-audit passed 3-completed 4-canceled)'''
);

-- sale_return_main: missing order_status (1-pending audit 2-audit passed 3-completed 4-canceled)
CALL psi_add_column_if_missing(
    'sale_return_main',
    'order_status',
    'TINYINT DEFAULT 1 COMMENT ''order status (1-pending audit 2-audit passed 3-completed 4-canceled)'''
);

-- sale_order_main: ensure order_status exists (1-pending audit 2-audit passed 3-out 4-canceled 5-completed)
-- This table likely already has the column; the procedure is idempotent.
CALL psi_add_column_if_missing(
    'sale_order_main',
    'order_status',
    'TINYINT DEFAULT 1 COMMENT ''order status (1-pending audit 2-audit passed 3-out 4-canceled 5-completed)'''
);

-- purchase_order_main: ensure order_status exists (1-pending audit 2-audited 3-canceled 4-completed)
-- This table likely already has the column; the procedure is idempotent.
CALL psi_add_column_if_missing(
    'purchase_order_main',
    'order_status',
    'TINYINT DEFAULT NULL COMMENT ''order status (1-pending audit 2-audited 3-canceled 4-completed)'''
);

-- cleanup: drop the helper procedure
DROP PROCEDURE IF EXISTS `psi_add_column_if_missing`;
