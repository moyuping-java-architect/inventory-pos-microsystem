-- ============================================
-- psi-modular 鍗曚綋鏁版嵁搴撳垵濮嬪寲鑴氭湰
-- 鑷姩鍚堝苟鑷悇妯″潡鐨?schema.sql 鍜?Flyway migration 鑴氭湰
-- 鎵€鏈夋ā鍧楃粺涓€浣跨敤 psi_modular 鏁版嵁搴?-- ============================================

CREATE DATABASE IF NOT EXISTS psi_modular
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE psi_modular;


-- ============================================
-- psi-common-starter-order-rule schema.sql
-- 鏉ユ簮: \psi-common-starter-order-rule\src\main\resources\schema.sql
-- ============================================

-- psi-common-starter-order-rule 通用草稿单据模块 建表脚本
-- 适用于采购、销售、退货、报损、报溢、盘点等所有业务单据的草稿
-- 通过 doc_type 字段区分不同单据类型，审批通过后转为各业务正式表

-- 单据主表（草稿表，审批通过后转为各业务的正式表）
CREATE TABLE IF NOT EXISTS doc_main_draft (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    shop_code VARCHAR(64) DEFAULT NULL COMMENT '商铺编码',
    shop_name VARCHAR(200) DEFAULT NULL COMMENT '商铺名称',
    doc_no VARCHAR(64) NOT NULL COMMENT '单据编号（自动生成）',
    doc_type VARCHAR(32) NOT NULL COMMENT '单据类型：PURCHASE_ORDER-采购订单 PURCHASE_IN-采购入库 PURCHASE_RETURN-采购退货 SALE_ORDER-销售订单 SALE_OUT-销售出库 SALE_RETURN-销售退货 STOCK_LOSS-报损单 STOCK_OVERFLOW-报溢单 STOCK_CHECK-盘点单 STOCK_TRANSFER-调拨单',
    status INT DEFAULT 0 COMMENT '单据状态：0-草稿 1-已提交 2-审批中 3-已审批 4-执行中 5-已完成 -1-已取消 -2-已驳回',
    creator_id VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
    creator_name VARCHAR(100) DEFAULT NULL COMMENT '创建人姓名',
    dept_id VARCHAR(64) DEFAULT NULL COMMENT '部门ID',
    dept_name VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
    partner_id VARCHAR(64) DEFAULT NULL COMMENT '供应商/客户ID（根据单据类型使用）',
    partner_code VARCHAR(64) DEFAULT NULL COMMENT '供应商/客户编码',
    partner_name VARCHAR(200) DEFAULT NULL COMMENT '供应商/客户名称',
    warehouse_id BIGINT DEFAULT NULL COMMENT '仓库ID',
    warehouse_code VARCHAR(64) DEFAULT NULL COMMENT '仓库编码',
    warehouse_name VARCHAR(100) DEFAULT NULL COMMENT '仓库名称',
    order_no VARCHAR(64) DEFAULT NULL COMMENT '关联订单号（用于入库/出库等关联上游单据）',
    sale_type INT DEFAULT NULL COMMENT '销售类型：1-普通销售 2-批发 3-零售',
    payment_type INT DEFAULT NULL COMMENT '付款方式：1-预付定金 2-货到付款 3-月结 4-现金 5-刷卡 6-赊销',
    currency_code VARCHAR(32) DEFAULT 'CNY' COMMENT '货币编码',
    exchange_rate DECIMAL(12,4) DEFAULT 1.0000 COMMENT '汇率',
    total_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '总金额（不含税）',
    tax_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '税额',
    discount_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '折扣金额',
    pay_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '实付/实收金额',
    item_count INT DEFAULT 0 COMMENT '明细数量',
    doc_date DATETIME DEFAULT NULL COMMENT '单据日期',
    delivery_date DATETIME DEFAULT NULL COMMENT '交货/预计到货日期',
    audit_status INT DEFAULT 0 COMMENT '审核状态：0-未审核 1-已审核 2-审核驳回',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    execute_time DATETIME DEFAULT NULL COMMENT '执行时间',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    doc_name VARCHAR(200) NOT NULL COMMENT '单据名称（必填，默认：单据类型+当天日期）',
    
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    ext_json TEXT COMMENT '扩展字段（JSON格式，存储单据类型特有字段如报损原因、调拨出入库等）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_uuid (data_uuid),
    UNIQUE KEY uk_doc_no (doc_no),
    INDEX idx_doc_type (doc_type),
    INDEX idx_status (status),
    INDEX idx_partner_id (partner_id),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_doc_date (doc_date),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_shop_code (shop_code),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据草稿主表（采购、销售、退货、报损、报溢、盘点等通用草稿）';

-- 单据明细表（草稿表）
CREATE TABLE IF NOT EXISTS doc_item_draft (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    shop_code VARCHAR(64) DEFAULT NULL COMMENT '商铺编码',
    shop_name VARCHAR(200) DEFAULT NULL COMMENT '商铺名称',
    doc_id BIGINT NOT NULL COMMENT '草稿主表ID',
    doc_no VARCHAR(64) DEFAULT NULL COMMENT '单据编号',
    goods_id BIGINT DEFAULT NULL COMMENT '商品ID',
    goods_code VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
    sku_code VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    sku_name VARCHAR(200) DEFAULT NULL COMMENT 'SKU名称',
    barcode VARCHAR(64) DEFAULT NULL COMMENT '条码',
    goods_name VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
    goods_spec VARCHAR(200) DEFAULT NULL COMMENT '商品规格',
    unit_code VARCHAR(32) DEFAULT NULL COMMENT '计量单位编码',
    goods_unit VARCHAR(32) DEFAULT NULL COMMENT '商品单位名称',
    conversion_rate DECIMAL(18,4) DEFAULT 1.0000 COMMENT '销售单位到库存基础单位的换算率',
    unit_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '单价（不含税）',
    quantity DECIMAL(18,2) DEFAULT 0.00 COMMENT '数量',
    amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '金额（不含税，单价*数量）',
    tax_rate DECIMAL(12,2) DEFAULT 0.00 COMMENT '税率(%)',
    tax_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '税额',
    discount_rate DECIMAL(12,2) DEFAULT 0.00 COMMENT '折扣率(%)',
    discount_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '折扣金额',
    
net_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '净金额（含税）',
    cost_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '成本价',
    cost_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '成本金额',
    pay_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '实付金额',
    stock_id BIGINT DEFAULT NULL COMMENT '库存ID',
    batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
    expiry_date VARCHAR(32) DEFAULT NULL COMMENT '有效期至',
    line_no INT DEFAULT 0 COMMENT '行号',
    
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_uuid (data_uuid),
    INDEX idx_doc_id (doc_id),
    INDEX idx_doc_no (doc_no),
    INDEX idx_goods_id (goods_id),
    INDEX idx_goods_code (goods_code),
    INDEX idx_shop_code (shop_code),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据明细草稿表';


-- ============================================
-- psi-finance schema.sql
-- 鏉ユ簮: \psi-finance\src\main\resources\schema.sql
-- ============================================

CREATE TABLE IF NOT EXISTS `finance_receivable` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `store_code` VARCHAR(64) NOT NULL COMMENT '门店编码',
    `store_name` VARCHAR(128) DEFAULT NULL COMMENT '门店名称',
    `customer_id` BIGINT DEFAULT NULL COMMENT '客户ID',
    `customer_code` VARCHAR(64) DEFAULT NULL COMMENT '客户编码',
    `customer_name` VARCHAR(128) DEFAULT NULL COMMENT '客户名称',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '总金额',
    `paid_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '已付金额',
    `remain_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '剩余金额',
    `source_no` VARCHAR(64) DEFAULT NULL COMMENT '来源单据编号',
    `source_type` VARCHAR(64) DEFAULT NULL COMMENT '来源单据类型',
    `bill_date` VARCHAR(20) DEFAULT NULL COMMENT '单据日期',
    `due_date` VARCHAR(20) DEFAULT NULL COMMENT '到期日期',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_store_code` (`store_code`),
    INDEX `idx_customer_code` (`customer_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收款表';

CREATE TABLE IF NOT EXISTS `finance_receivable_pay` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `store_code` VARCHAR(64) NOT NULL COMMENT '门店编码',
    `store_name` VARCHAR(128) DEFAULT NULL COMMENT '门店名称',
    `receivable_id` BIGINT NOT NULL COMMENT '应收款ID',
    `customer_code` VARCHAR(64) DEFAULT NULL COMMENT '客户编码',
    `customer_name` VARCHAR(128) DEFAULT NULL COMMENT '客户名称',
    `pay_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '付款金额',
    `pay_method` VARCHAR(32) DEFAULT NULL COMMENT '付款方式(CASH/WECHAT/ALIPAY/BANK)',
    `pay_no` VARCHAR(64) DEFAULT NULL COMMENT '付款流水号',
    `pay_date` VARCHAR(20) DEFAULT NULL COMMENT '付款日期',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_store_code` (`store_code`),
    INDEX `idx_receivable_id` (`receivable_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收款还款记录表';

CREATE TABLE IF NOT EXISTS `finance_payable` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `store_code` VARCHAR(64) NOT NULL COMMENT '门店编码',
    `store_name` VARCHAR(128) DEFAULT NULL COMMENT '门店名称',
    `supplier_id` BIGINT DEFAULT NULL COMMENT '供应商ID',
    `supplier_code` VARCHAR(64) DEFAULT NULL COMMENT '供应商编码',
    `supplier_name` VARCHAR(128) DEFAULT NULL COMMENT '供应商名称',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '总金额',
    `paid_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '已付金额',
    `remain_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '剩余金额',
    `source_no` VARCHAR(64) DEFAULT NULL COMMENT '来源单据编号',
    `source_type` VARCHAR(64) DEFAULT NULL COMMENT '来源单据类型',
    `bill_date` VARCHAR(20) DEFAULT NULL COMMENT '单据日期',
    `due_date` VARCHAR(20) DEFAULT NULL COMMENT '到期日期',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_store_code` (`store_code`),
    INDEX `idx_supplier_code` (`supplier_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应付款表';

CREATE TABLE IF NOT EXISTS `finance_payable_pay` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `store_code` VARCHAR(64) NOT NULL COMMENT '门店编码',
    `store_name` VARCHAR(128) DEFAULT NULL COMMENT '门店名称',
    `payable_id` BIGINT NOT NULL COMMENT '应付款ID',
    `supplier_code` VARCHAR(64) DEFAULT NULL COMMENT '供应商编码',
    `supplier_name` VARCHAR(128) DEFAULT NULL COMMENT '供应商名称',
    `pay_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '付款金额',
    `pay_method` VARCHAR(32) DEFAULT NULL COMMENT '付款方式(CASH/WECHAT/ALIPAY/BANK)',
    `pay_no` VARCHAR(64) DEFAULT NULL COMMENT '付款流水号',
    `pay_date` VARCHAR(20) DEFAULT NULL COMMENT '付款日期',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_store_code` (`store_code`),
    INDEX `idx_payable_id` (`payable_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应付款付款记录表';

CREATE TABLE IF NOT EXISTS `finance_account` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `store_code` VARCHAR(64) NOT NULL COMMENT '门店编码',
    `store_name` VARCHAR(128) DEFAULT NULL COMMENT '门店名称',
    `account_type` VARCHAR(32) NOT NULL COMMENT '账户类型(CASH/WECHAT/ALIPAY/BANK)',
    `account_name` VARCHAR(128) DEFAULT NULL COMMENT '账户名称',
    `account_no` VARCHAR(64) DEFAULT NULL COMMENT '账户号码',
    `balance` DECIMAL(18,4) DEFAULT NULL COMMENT '账户余额',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_store_account` (`store_code`, `account_type`),
    INDEX `idx_store_code` (`store_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金账户表';

CREATE TABLE IF NOT EXISTS `finance_account_flow` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `store_code` VARCHAR(64) NOT NULL COMMENT '门店编码',
    `store_name` VARCHAR(128) DEFAULT NULL COMMENT '门店名称',
    `account_type` VARCHAR(32) NOT NULL COMMENT '账户类型',
    `account_name` VARCHAR(128) DEFAULT NULL COMMENT '账户名称',
    `flow_type` TINYINT DEFAULT 0 COMMENT '流水类型(1:收入 2:支出)',
    `in_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '收入金额',
    `out_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '支出金额',
    `balance_before` DECIMAL(18,4) DEFAULT NULL COMMENT '变动前余额',
    `balance_after` DECIMAL(18,4) DEFAULT NULL COMMENT '变动后余额',
    `source_no` VARCHAR(64) DEFAULT NULL COMMENT '来源单据编号',
    `source_type` VARCHAR(64) DEFAULT NULL COMMENT '来源单据类型',
    `pay_no` VARCHAR(64) DEFAULT NULL COMMENT '支付流水号',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_store_code` (`store_code`),
    INDEX `idx_account_type` (`account_type`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金账户流水表';

CREATE TABLE IF NOT EXISTS `finance_daily_ledger` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `store_code` VARCHAR(64) NOT NULL COMMENT '门店编码',
    `store_name` VARCHAR(128) DEFAULT NULL COMMENT '门店名称',
    `ledger_date` VARCHAR(20) NOT NULL COMMENT '台账日期',
    `sale_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '销售金额',
    `cost_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '成本金额',
    `profit_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '利润金额',
    `cash_in` DECIMAL(18,4) DEFAULT NULL COMMENT '现金收入',
    `cash_out` DECIMAL(18,4) DEFAULT NULL COMMENT '现金支出',
    `transfer_in` DECIMAL(18,4) DEFAULT NULL COMMENT '转账收入',
    `transfer_out` DECIMAL(18,4) DEFAULT NULL COMMENT '转账支出',
    `receivable_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '应收金额',
    `payable_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '应付金额',
    `beginning_balance` DECIMAL(18,4) DEFAULT NULL COMMENT '期初余额',
    `ending_balance` DECIMAL(18,4) DEFAULT NULL COMMENT '期末余额',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_store_date` (`store_code`, `ledger_date`),
    INDEX `idx_store_code` (`store_code`),
    INDEX `idx_ledger_date` (`ledger_date`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店台账日报表';

CREATE TABLE IF NOT EXISTS `finance_daily_close` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `store_code` VARCHAR(64) NOT NULL COMMENT '门店编码',
    `store_name` VARCHAR(128) DEFAULT NULL COMMENT '门店名称',
    `close_date` VARCHAR(20) NOT NULL COMMENT '日结日期',
    `sale_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '销售金额',
    `cost_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '成本金额',
    `profit_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '利润金额',
    `cash_in` DECIMAL(18,4) DEFAULT NULL COMMENT '现金收入',
    `cash_out` DECIMAL(18,4) DEFAULT NULL COMMENT '现金支出',
    `transfer_in` DECIMAL(18,4) DEFAULT NULL COMMENT '转账收入',
    `transfer_out` DECIMAL(18,4) DEFAULT NULL COMMENT '转账支出',
    `receivable_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '应收金额',
    `payable_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '应付金额',
    `cash_balance` DECIMAL(18,4) DEFAULT NULL COMMENT '现金账户余额',
    `wechat_balance` DECIMAL(18,4) DEFAULT NULL COMMENT '微信账户余额',
    `alipay_balance` DECIMAL(18,4) DEFAULT NULL COMMENT '支付宝账户余额',
    `bank_balance` DECIMAL(18,4) DEFAULT NULL COMMENT '对公账户余额',
    `total_balance` DECIMAL(18,4) DEFAULT NULL COMMENT '总余额',
    `close_by` VARCHAR(64) DEFAULT NULL COMMENT '日结人',
    `close_time` VARCHAR(30) DEFAULT NULL COMMENT '日结时间',
    `close_status` TINYINT DEFAULT 0 COMMENT '日结状态(0:未日结 1:已日结 2:已撤销)',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_store_close_date` (`store_code`, `close_date`),
    INDEX `idx_store_code` (`store_code`),
    INDEX `idx_close_date` (`close_date`),
    INDEX `idx_close_status` (`close_status`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店日结表';


-- ============================================
-- psi-flow schema.sql
-- 鏉ユ簮: \psi-flow\src\main\resources\schema.sql
-- ============================================

-- psi-flow 工作流微服务 建表脚本

-- 流程定义表
CREATE TABLE IF NOT EXISTS `wf_process_definition` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_key` VARCHAR(100) NOT NULL COMMENT '流程唯一标识',
    `process_name` VARCHAR(200) NOT NULL COMMENT '流程名称',
    `version` INT DEFAULT 1 COMMENT '版本号',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_process_key` (`process_key`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- 流程节点表
CREATE TABLE IF NOT EXISTS `wf_process_node` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `node_key` VARCHAR(100) NOT NULL COMMENT '节点标识',
    `node_name` VARCHAR(200) NOT NULL COMMENT '节点名称',
    `node_type` TINYINT DEFAULT 1 COMMENT '节点类型 1-审批 2-条件 3-抄送 4-结束',
    `approve_type` TINYINT DEFAULT 1 COMMENT '审批类型 1-单人 2-会签 3-或签',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `config` TEXT COMMENT '节点自定义配置（JSON格式）',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_process_def_id` (`process_def_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点表';

-- 节点流转关系表
CREATE TABLE IF NOT EXISTS `wf_process_relation` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `from_node_id` BIGINT NOT NULL COMMENT '来源节点ID',
    `to_node_id` BIGINT NOT NULL COMMENT '目标节点ID',
    `condition_expr` VARCHAR(500) DEFAULT NULL COMMENT 'EL条件表达式',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_process_def_id` (`process_def_id`),
    INDEX `idx_from_node` (`from_node_id`),
    INDEX `idx_to_node` (`to_node_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点流转关系表';

-- 流程条件配置表
CREATE TABLE IF NOT EXISTS `wf_process_condition_config` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_def_id` BIGINT NOT NULL COMMENT '关联流程定义ID',
    `condition_name` VARCHAR(100) NOT NULL COMMENT '条件名称',
    `condition_key` VARCHAR(100) NOT NULL COMMENT '变量key',
    `condition_type` VARCHAR(20) DEFAULT 'string' COMMENT '类型 number/string/boolean',
    `compare_type` VARCHAR(10) DEFAULT '=' COMMENT '运算符 > < >= <= =',
    `default_value` VARCHAR(200) DEFAULT NULL COMMENT '默认值',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_process_def_id` (`process_def_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程条件配置表';

-- 流程实例表（不继承BaseEntity，使用自定义ID）
CREATE TABLE IF NOT EXISTS `wf_process_instance` (
    `id` VARCHAR(64) NOT NULL COMMENT '实例ID（自定义生成）',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_def_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `process_key` VARCHAR(100) NOT NULL COMMENT '流程标识',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '流程标题',
    `start_user_id` VARCHAR(64) DEFAULT NULL COMMENT '发起人ID',
    `start_user_name` VARCHAR(100) DEFAULT NULL COMMENT '发起人姓名',
    `current_node_id` BIGINT DEFAULT NULL COMMENT '当前节点ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0-待审批 1-审批中 2-已通过 3-已驳回 4-已撤销',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_process_def_id` (`process_def_id`),
    INDEX `idx_start_user` (`start_user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

-- 流程任务表（不继承BaseEntity，使用自定义ID）
CREATE TABLE IF NOT EXISTS `wf_task` (
    `id` VARCHAR(64) NOT NULL COMMENT '任务ID（自定义生成）',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_instance_id` VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    `node_id` BIGINT DEFAULT NULL COMMENT '节点ID',
    `task_name` VARCHAR(200) DEFAULT NULL COMMENT '任务名称',
    `handler_user_id` VARCHAR(64) DEFAULT NULL COMMENT '处理人ID',
    `handler_user_name` VARCHAR(100) DEFAULT NULL COMMENT '处理人姓名',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0-待处理 1-已处理 2-已跳过',
    `handle_note` VARCHAR(500) DEFAULT NULL COMMENT '处理意见',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_process_instance_id` (`process_instance_id`),
    INDEX `idx_handler` (`handler_user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程任务表';

-- 流程业务关联表
CREATE TABLE IF NOT EXISTS `wf_process_instance_biz` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_instance_id` VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    `biz_type` VARCHAR(100) DEFAULT NULL COMMENT '业务类型',
    `biz_id` VARCHAR(100) DEFAULT NULL COMMENT '业务ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_process_instance_id` (`process_instance_id`),
    INDEX `idx_biz_type_biz_id` (`biz_type`, `biz_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程业务关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `wf_operation_log` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_instance_id` VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    `operator_id` VARCHAR(64) DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    `operate_type` TINYINT DEFAULT NULL COMMENT '操作类型 1-发起 2-同意 3-驳回 4-撤销',
    `operate_content` VARCHAR(500) DEFAULT NULL COMMENT '操作内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_process_instance_id` (`process_instance_id`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 抄送记录表
CREATE TABLE IF NOT EXISTS `wf_cc_log` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `process_instance_id` VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    `cc_user_id` VARCHAR(64) DEFAULT NULL COMMENT '抄送用户ID',
    `cc_user_name` VARCHAR(100) DEFAULT NULL COMMENT '抄送用户姓名',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1-未读 2-已读',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_process_instance_id` (`process_instance_id`),
    INDEX `idx_cc_user_id` (`cc_user_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抄送记录表';

-- ============================================================
-- 草稿单据表（通用草稿，适用于采购、销售、退货、报损、报溢、盘点等所有业务单据）
-- 通过 doc_type 字段区分不同单据类型，审批通过后转为各业务的正式表
-- ============================================================

-- 单据主表（草稿表）
CREATE TABLE IF NOT EXISTS doc_main_draft (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    shop_code VARCHAR(64) DEFAULT NULL COMMENT '商铺编码',
    shop_name VARCHAR(200) DEFAULT NULL COMMENT '商铺名称',
    doc_no VARCHAR(64) NOT NULL COMMENT '单据编号（自动生成）',
    doc_type VARCHAR(32) NOT NULL COMMENT '单据类型：PURCHASE_ORDER-采购订单 PURCHASE_IN-采购入库 PURCHASE_RETURN-采购退货 SALE_ORDER-销售订单 SALE_OUT-销售出库 SALE_RETURN-销售退货 STOCK_LOSS-报损单 STOCK_OVERFLOW-报溢单 STOCK_CHECK-盘点单 STOCK_TRANSFER-调拨单',
    status INT DEFAULT 0 COMMENT '单据状态：0-草稿 1-已提交 2-审批中 3-已审批 4-执行中 5-已完成 -1-已取消 -2-已驳回',
    creator_id VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
    creator_name VARCHAR(100) DEFAULT NULL COMMENT '创建人姓名',
    dept_id VARCHAR(64) DEFAULT NULL COMMENT '部门ID',
    dept_name VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
    partner_id VARCHAR(64) DEFAULT NULL COMMENT '供应商/客户ID（根据单据类型使用）',
    partner_code VARCHAR(64) DEFAULT NULL COMMENT '供应商/客户编码',
    partner_name VARCHAR(200) DEFAULT NULL COMMENT '供应商/客户名称',
    warehouse_id BIGINT DEFAULT NULL COMMENT '仓库ID',
    warehouse_code VARCHAR(64) DEFAULT NULL COMMENT '仓库编码',
    warehouse_name VARCHAR(100) DEFAULT NULL COMMENT '仓库名称',
    sale_type INT DEFAULT NULL COMMENT '销售类型：1-普通销售 2-批发 3-零售',
    payment_type INT DEFAULT NULL COMMENT '付款方式：1-预付定金 2-货到付款 3-月结 4-现金 5-刷卡 6-赊销',
    currency_code VARCHAR(32) DEFAULT 'CNY' COMMENT '货币编码',
    exchange_rate DECIMAL(12,4) DEFAULT 1.0000 COMMENT '汇率',
    total_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '总金额（不含税）',
    tax_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '税额',
    discount_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '折扣金额',
    pay_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '实付/实收金额',
    item_count INT DEFAULT 0 COMMENT '明细数量',
    doc_date DATETIME DEFAULT NULL COMMENT '单据日期',
    delivery_date DATETIME DEFAULT NULL COMMENT '交货/预计到货日期',
    audit_status INT DEFAULT 0 COMMENT '审核状态：0-未审核 1-已审核 2-审核驳回',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    execute_time DATETIME DEFAULT NULL COMMENT '执行时间',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    ext_json TEXT COMMENT '扩展字段（JSON格式，存储单据类型特有字段如报损原因、调拨出入库等）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_uuid (data_uuid),
    UNIQUE KEY uk_doc_no (doc_no),
    INDEX idx_doc_type (doc_type),
    INDEX idx_status (status),
    INDEX idx_partner_id (partner_id),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_doc_date (doc_date),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_shop_code (shop_code),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据草稿主表（采购、销售、退货、报损、报溢、盘点等通用草稿）';

-- 单据明细表（草稿表）
CREATE TABLE IF NOT EXISTS doc_item_draft (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    shop_code VARCHAR(64) DEFAULT NULL COMMENT '商铺编码',
    shop_name VARCHAR(200) DEFAULT NULL COMMENT '商铺名称',
    doc_id BIGINT NOT NULL COMMENT '草稿主表ID',
    doc_no VARCHAR(64) DEFAULT NULL COMMENT '单据编号',
    goods_id BIGINT DEFAULT NULL COMMENT '商品ID',
    goods_code VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
    goods_name VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
    goods_spec VARCHAR(200) DEFAULT NULL COMMENT '商品规格',
    unit_code VARCHAR(32) DEFAULT NULL COMMENT '计量单位编码',
    goods_unit VARCHAR(32) DEFAULT NULL COMMENT '商品单位名称',
    unit_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '单价（不含税）',
    quantity DECIMAL(18,2) DEFAULT 0.00 COMMENT '数量',
    amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '金额（不含税，单价*数量）',
    tax_rate DECIMAL(12,2) DEFAULT 0.00 COMMENT '税率(%)',
    tax_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '税额',
    discount_rate DECIMAL(12,2) DEFAULT 0.00 COMMENT '折扣率(%)',
    discount_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '折扣金额',
    net_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '净金额（含税）',
    cost_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '成本价',
    cost_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '成本金额',
    pay_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '实付金额',
    stock_id BIGINT DEFAULT NULL COMMENT '库存ID',
    batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
    expiry_date VARCHAR(32) DEFAULT NULL COMMENT '有效期至',
    line_no INT DEFAULT 0 COMMENT '行号',
    
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_uuid (data_uuid),
    INDEX idx_doc_id (doc_id),
    INDEX idx_doc_no (doc_no),
    INDEX idx_goods_id (goods_id),
    INDEX idx_goods_code (goods_code),
    INDEX idx_shop_code (shop_code),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据明细草稿表';


-- ============================================
-- psi-goods schema.sql
-- 鏉ユ簮: \psi-goods\src\main\resources\schema.sql
-- ============================================

CREATE TABLE IF NOT EXISTS `goods` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `goods_code` VARCHAR(64) NOT NULL COMMENT '商品编码',
    `goods_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
    `goods_name_en` VARCHAR(256) DEFAULT NULL COMMENT '商品英文名称',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `brand_id` BIGINT DEFAULT NULL COMMENT '品牌ID',
    `goods_spec` VARCHAR(256) DEFAULT NULL COMMENT '商品规格',
    `unit` VARCHAR(32) DEFAULT NULL COMMENT '计量单位',
    `image_url` VARCHAR(512) DEFAULT NULL COMMENT '主图URL',
    `images` TEXT DEFAULT NULL COMMENT '图片列表（JSON数组）',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `features` TEXT DEFAULT NULL COMMENT '商品特性（JSON数组）',
    `stock_qty` INT DEFAULT NULL COMMENT '库存数量',
    `supplier_code` VARCHAR(64) DEFAULT NULL COMMENT '供应商编码',
    `weight` INT DEFAULT NULL COMMENT '重量',
    `weight_unit` VARCHAR(32) DEFAULT NULL COMMENT '重量单位',
    `package_spec` VARCHAR(256) DEFAULT NULL COMMENT '包装规格',
    `certification` VARCHAR(512) DEFAULT NULL COMMENT '认证信息',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0:启用 1:禁用)',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_goods_code` (`goods_code`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_brand_id` (`brand_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主表';

CREATE TABLE IF NOT EXISTS `goods_category` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(64) NOT NULL COMMENT '分类编码',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID',
    `level` INT DEFAULT 1 COMMENT '分类层级',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `icon` VARCHAR(512) DEFAULT NULL COMMENT '分类图标',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0:启用 1:禁用)',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS `goods_brand` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `brand_name` VARCHAR(128) NOT NULL COMMENT '品牌名称',
    `brand_code` VARCHAR(64) NOT NULL COMMENT '品牌编码',
    `brand_logo` VARCHAR(512) DEFAULT NULL COMMENT '品牌Logo',
    `brand_desc` VARCHAR(500) DEFAULT NULL COMMENT '品牌描述',
    `website` VARCHAR(256) DEFAULT NULL COMMENT '品牌官网',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0:启用 1:禁用)',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_brand_code` (`brand_code`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品品牌表';

CREATE TABLE IF NOT EXISTS `goods_unit` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `unit_code` VARCHAR(64) NOT NULL COMMENT '单位编码',
    `unit_name` VARCHAR(128) NOT NULL COMMENT '单位名称',
    `unit_symbol` VARCHAR(32) DEFAULT NULL COMMENT '单位符号（如：kg、m、件）',
    `unit_type` VARCHAR(32) DEFAULT NULL COMMENT '单位类型（WEIGHT-重量单位，VOLUME-体积单位，COUNT-计数单位，OTHER-其他）',
    `conversion_rate` DECIMAL(18,4) DEFAULT NULL COMMENT '换算比例（相对于基础单位的换算系数）',
    `base_unit_id` BIGINT DEFAULT NULL COMMENT '基础单位ID（父单位ID）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0:启用 1:禁用)',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_unit_code` (`unit_code`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_base_unit_id` (`base_unit_id`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品单位表';

CREATE TABLE IF NOT EXISTS `goods_sku` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `spec_values` VARCHAR(512) DEFAULT NULL COMMENT '规格值（逗号分隔）',
    `spec_json` TEXT DEFAULT NULL COMMENT '规格JSON',
    `cost_price` DECIMAL(18,4) DEFAULT NULL COMMENT '成本价',
    `sale_price` DECIMAL(18,4) DEFAULT NULL COMMENT '销售价',
    `market_price` DECIMAL(18,4) DEFAULT NULL COMMENT '市场价',
    `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条形码',
    `image_url` VARCHAR(512) DEFAULT NULL COMMENT '图片URL',
    `stock_qty` INT DEFAULT NULL COMMENT '库存数量',
    `min_stock_qty` INT DEFAULT NULL COMMENT '最低库存',
    `max_stock_qty` INT DEFAULT NULL COMMENT '最高库存',
    `supplier_code` VARCHAR(64) DEFAULT NULL COMMENT '供应商编码',
    `weight` DECIMAL(18,4) DEFAULT NULL COMMENT '重量',
    `weight_unit` VARCHAR(32) DEFAULT NULL COMMENT '重量单位',
    `volume` DECIMAL(18,4) DEFAULT NULL COMMENT '体积',
    `volume_unit` VARCHAR(32) DEFAULT NULL COMMENT '体积单位',
    `package_spec` VARCHAR(256) DEFAULT NULL COMMENT '包装规格',
    `sales_count` INT DEFAULT 0 COMMENT '销量',
    `base_unit` VARCHAR(32) DEFAULT NULL COMMENT '基础单位（如：kg、g、件）',
    `sale_unit` VARCHAR(32) DEFAULT NULL COMMENT '销售单位（如：包、箱、盒）',
    `unit_conversion` VARCHAR(64) DEFAULT NULL COMMENT '单位换算（如：0.4kg/包）',
    `goods_unify_code` VARCHAR(64) DEFAULT NULL COMMENT '商品统一编码（同品多批次共用同一个编码）',
    `tax_rate` DECIMAL(5,4) DEFAULT 0.1600 COMMENT 'VAT税率（如0.1600表示16%）',
    `is_tax_inclusive` TINYINT DEFAULT 0 COMMENT '标价是否含税(0:否 1:是)',
    `sale_price_usd` DECIMAL(12,4) DEFAULT 0.0000 COMMENT 'USD销售价',
    `cost_price_usd` DECIMAL(12,4) DEFAULT 0.0000 COMMENT 'USD成本价',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0:启用 1:禁用)',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_goods_id` (`goods_id`),
    INDEX `idx_barcode` (`barcode`),
    INDEX `idx_goods_unify_code` (`goods_unify_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

CREATE TABLE IF NOT EXISTS `goods_sku_sale_unit` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `goods_unify_code` VARCHAR(64) DEFAULT NULL COMMENT '商品统一编码（同品多批次共用同一个编码）',
    `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条码',
    `goods_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
    `category_id` BIGINT DEFAULT NULL COMMENT '商品分类ID',
    `brand_id` BIGINT DEFAULT NULL COMMENT '商品品牌ID',
    `image_url` VARCHAR(512) DEFAULT NULL COMMENT '商品图片URL',
    `sale_unit_id` BIGINT DEFAULT NULL COMMENT '销售单位ID',
    `sale_unit_name` VARCHAR(128) DEFAULT NULL COMMENT '销售单位名称',
    `sale_unit_symbol` VARCHAR(32) DEFAULT NULL COMMENT '销售单位符号',
    `conversion_rate` DECIMAL(18,4) DEFAULT NULL COMMENT '换算比例（相对于SKU基础单位）',
    `package_spec` VARCHAR(256) DEFAULT NULL COMMENT '包装规格描述',
    `sale_price` DECIMAL(18,4) DEFAULT NULL COMMENT '销售价格',
    `cost_price` DECIMAL(18,4) DEFAULT NULL COMMENT '成本价格',
    `tax_rate` DECIMAL(5,4) DEFAULT 0.1600 COMMENT 'VAT税率（如0.1600表示16%）',
    `is_tax_inclusive` TINYINT DEFAULT 0 COMMENT '标价是否含税(0:否 1:是)',
    `sale_price_usd` DECIMAL(12,4) DEFAULT 0.0000 COMMENT 'USD销售价',
    `batch_managed` TINYINT DEFAULT 0 COMMENT '是否管理批次/效期(0:否 1:是)',
    `stock_qty` DECIMAL(18,4) DEFAULT NULL COMMENT '库存数量',
    `min_stock_qty` DECIMAL(18,4) DEFAULT NULL COMMENT '最低库存',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认销售单位(0:否 1:是)',
    `status` TINYINT DEFAULT 0 COMMENT '是否启用(0:禁用 1:启用)',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_sku_id` (`sku_id`),
    INDEX `idx_barcode` (`barcode`),
    INDEX `idx_goods_unify_code` (`goods_unify_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU销售单位表';

CREATE TABLE IF NOT EXISTS `goods_adjust_price_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `adjust_no` VARCHAR(64) NOT NULL COMMENT '调价单号',
    `doc_name` VARCHAR(128) DEFAULT NULL COMMENT '单据名称',
    `shop_code` VARCHAR(64) DEFAULT NULL COMMENT '商铺编码',
    `shop_name` VARCHAR(128) DEFAULT NULL COMMENT '商铺名称',
    `adjust_date` VARCHAR(32) DEFAULT NULL COMMENT '调价日期',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '总金额',
    `item_count` INT DEFAULT 0 COMMENT '明细项数',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `status` TINYINT DEFAULT 0 COMMENT '状态(0:草稿 1:已提交 2:已审核)',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_adjust_no` (`adjust_no`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_status` (`status`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品调价单主表';

CREATE TABLE IF NOT EXISTS `goods_adjust_price_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `adjust_id` BIGINT NOT NULL COMMENT '调价单ID',
    `goods_code` VARCHAR(64) NOT NULL COMMENT '商品编码',
    `goods_name` VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    `goods_spec` VARCHAR(256) DEFAULT NULL COMMENT '商品规格',
    `goods_unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
    `old_price` DECIMAL(18,4) DEFAULT NULL COMMENT '原销售价',
    `new_price` DECIMAL(18,4) NOT NULL COMMENT '新销售价',
    `quantity` DECIMAL(18,4) DEFAULT NULL COMMENT '数量',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT '金额',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    INDEX `idx_adjust_id` (`adjust_id`),
    INDEX `idx_sku_code` (`sku_code`),
    INDEX `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品调价单明细表';


-- ============================================
-- psi-member schema.sql
-- 鏉ユ簮: \psi-member\src\main\resources\schema.sql
-- ============================================

CREATE TABLE IF NOT EXISTS `member_level` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `level_name` varchar(50) NOT NULL COMMENT '等级名称',
  `level` int NOT NULL DEFAULT 1 COMMENT '等级值',
  `discount` decimal(5,2) DEFAULT NULL COMMENT '折扣(%)',
  `min_consume` decimal(12,2) DEFAULT NULL COMMENT '升级所需最低消费',
  `min_points` int DEFAULT NULL COMMENT '升级所需最低积分',
  `point_rate` decimal(5,2) DEFAULT 1.00 COMMENT '积分倍率',
  `level_icon` varchar(255) DEFAULT NULL COMMENT '等级图标',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标记',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level` (`level`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

CREATE TABLE IF NOT EXISTS `member_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_no` varchar(32) NOT NULL COMMENT '会员编号',
  `member_name` varchar(100) NOT NULL COMMENT '会员姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `level_id` bigint DEFAULT NULL COMMENT '等级ID',
  `level_name` varchar(50) DEFAULT NULL COMMENT '等级名称',
  `balance` decimal(12,2) DEFAULT 0.00 COMMENT '储值余额',
  `points` int DEFAULT 0 COMMENT '积分',
  `total_consume` decimal(12,2) DEFAULT 0.00 COMMENT '累计消费',
  `total_orders` int DEFAULT 0 COMMENT '累计订单数',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` tinyint DEFAULT 1 COMMENT '状态 1正常 0冻结',
  `register_time` datetime DEFAULT NULL COMMENT '注册时间',
  `last_consume_time` datetime DEFAULT NULL COMMENT '最后消费时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `shop_id` bigint DEFAULT NULL COMMENT '门店ID',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标记',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_no` (`member_no`, `del_flag`),
  KEY `idx_phone` (`phone`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员信息表';

CREATE TABLE IF NOT EXISTS `member_balance_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `member_no` varchar(32) DEFAULT NULL COMMENT '会员编号',
  `type` tinyint NOT NULL COMMENT '类型 1充值 2消费 3退款',
  `amount` decimal(12,2) NOT NULL COMMENT '变动金额',
  `before_balance` decimal(12,2) DEFAULT NULL COMMENT '变动前余额',
  `after_balance` decimal(12,2) DEFAULT NULL COMMENT '变动后余额',
  `source_no` varchar(64) DEFAULT NULL COMMENT '来源单号',
  `source_type` varchar(32) DEFAULT NULL COMMENT '来源类型',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标记',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_source_no` (`source_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员储值流水表';

CREATE TABLE IF NOT EXISTS `member_point_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `member_no` varchar(32) DEFAULT NULL COMMENT '会员编号',
  `type` tinyint NOT NULL COMMENT '类型 1获得 2消耗',
  `points` int NOT NULL COMMENT '变动积分',
  `before_points` int DEFAULT NULL COMMENT '变动前积分',
  `after_points` int DEFAULT NULL COMMENT '变动后积分',
  `source_no` varchar(64) DEFAULT NULL COMMENT '来源单号',
  `source_type` varchar(32) DEFAULT NULL COMMENT '来源类型',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标记',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_source_no` (`source_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员积分流水表';

INSERT INTO `member_level` (`level_name`, `level`, `discount`, `min_consume`, `min_points`, `point_rate`, `description`, `sort_order`) VALUES
('普通会员', 1, 100.00, 0.00, 0, 1.00, '注册即成为普通会员', 1),
('银卡会员', 2, 95.00, 1000.00, 1000, 1.50, '累计消费1000或积分达到1000', 2),
('金卡会员', 3, 90.00, 5000.00, 5000, 2.00, '累计消费5000或积分达到5000', 3),
('钻石会员', 4, 85.00, 20000.00, 20000, 3.00, '累计消费20000或积分达到20000', 4);



-- ============================================
-- psi-purchase schema.sql
-- 鏉ユ簮: \psi-purchase\src\main\resources\schema.sql
-- ============================================

CREATE TABLE IF NOT EXISTS `purchase_order_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '閲囪喘璁㈠崟缂栧彿',
    `doc_name` VARCHAR(200) NOT NULL COMMENT '鍗曟嵁鍚嶇О锛堝繀濉紝榛樿锛氬崟鎹被鍨�+褰撳ぉ鏃ユ湡锛�',
    `supplier_id` BIGINT DEFAULT NULL COMMENT '渚涘簲鍟咺D',
    `supplier_code` VARCHAR(64) DEFAULT NULL COMMENT '渚涘簲鍟嗙紪鐮',
    `supplier_name` VARCHAR(255) DEFAULT NULL COMMENT '渚涘簲鍟嗗悕绉',
    `order_date` VARCHAR(20) DEFAULT NULL COMMENT '璁㈠崟鏃ユ湡',
    `delivery_date` VARCHAR(20) DEFAULT NULL COMMENT '棰勮浜よ揣鏃ユ湡',
    `payment_type` TINYINT DEFAULT NULL COMMENT '浠樻鏂瑰紡锛?-棰勪粯瀹氶噾 2-璐у埌浠樻 3-鏈堢粨',
    `currency_code` VARCHAR(10) DEFAULT NULL COMMENT '璐у竵缂栫爜',
    `exchange_rate` DECIMAL(12,4) DEFAULT NULL COMMENT '姹囩巼',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '璁㈠崟鎬婚噾棰濓紙涓嶅惈绋庯級',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `discount_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '鎶樻墸閲戦',
    `pay_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '瀹為檯浠樻閲戦',
    `order_status` TINYINT DEFAULT NULL COMMENT '璁㈠崟鐘舵€侊細1-寰呭鏍?2-宸插鏍?3-宸插彇娑?4-宸插畬鎴',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `audit_status` TINYINT DEFAULT NULL COMMENT '瀹℃牳鐘舵€侊細0-鏈鏍?1-宸插鏍?2-瀹℃牳椹冲洖',
    `audit_time` DATETIME DEFAULT NULL COMMENT '瀹℃牳鏃堕棿',
    `audit_by` BIGINT DEFAULT NULL COMMENT '瀹℃牳浜篒D',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_supplier_code` (`supplier_code`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閲囪喘璁㈠崟涓昏〃';

CREATE TABLE IF NOT EXISTS `purchase_order_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `order_id` BIGINT NOT NULL COMMENT '璁㈠崟涓昏〃ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '璁㈠崟缂栧彿',
    `item_no` INT DEFAULT NULL COMMENT '琛屽彿',
    `goods_id` BIGINT DEFAULT NULL COMMENT '鍟嗗搧ID',
    `goods_code` VARCHAR(64) DEFAULT NULL COMMENT '鍟嗗搧缂栫爜',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'SKU名称',
    `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条码',
    `goods_name` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧鍚嶇О',
    `goods_spec` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧瑙勬牸鍨嬪彿',
    `unit_code` VARCHAR(32) DEFAULT NULL COMMENT '璁￠噺鍗曚綅缂栫爜',
    `goods_unit` VARCHAR(32) DEFAULT NULL COMMENT '商品单位名称',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT '销售单位到库存基础单位的换算率',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '閲囪喘鏁伴噺',
    `unit_price` DECIMAL(18,4) NOT NULL COMMENT '鍗曚环锛堜笉鍚◣锛',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT '閲戦锛堜笉鍚◣锛',
    `tax_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '绋庣巼',
    `is_tax_inclusive` TINYINT DEFAULT 0 COMMENT '标价是否含税(0:否 1:是)',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `discount_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '鎶樻墸鐜',
    `discount_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '鎶樻墸閲戦',
    `net_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '鍑€閲戦锛堝惈绋庯級',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_goods_code` (`goods_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閲囪喘璁㈠崟鏄庣粏';

CREATE TABLE IF NOT EXISTS `purchase_order_ext` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `order_id` BIGINT NOT NULL COMMENT '璁㈠崟涓昏〃ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '璁㈠崟缂栧彿',
    `ext_key` VARCHAR(128) NOT NULL COMMENT '鎵╁睍瀛楁閿',
    `ext_value` TEXT DEFAULT NULL COMMENT '鎵╁睍瀛楁鍊',
    `ext_desc` VARCHAR(255) DEFAULT NULL COMMENT '鎵╁睍瀛楁鎻忚堪',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_ext_key` (`ext_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閲囪喘璁㈠崟鎵╁睍';

CREATE TABLE IF NOT EXISTS `purchase_in_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `in_no` VARCHAR(64) NOT NULL COMMENT '鍏ュ簱鍗曠紪鍙',
    `doc_name` VARCHAR(200) NOT NULL COMMENT '鍗曟嵁鍚嶇О锛堝繀濉紝榛樿锛氬崟鎹被鍨�+褰撳ぉ鏃ユ湡锛�',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈閲囪喘璁㈠崟缂栧彿',
    `supplier_id` BIGINT DEFAULT NULL COMMENT '渚涘簲鍟咺D',
    `supplier_code` VARCHAR(64) DEFAULT NULL COMMENT '渚涘簲鍟嗙紪鐮',
    `supplier_name` VARCHAR(255) DEFAULT NULL COMMENT '渚涘簲鍟嗗悕绉',
    `in_date` VARCHAR(20) DEFAULT NULL COMMENT '鍏ュ簱鏃ユ湡',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT '浠撳簱缂栫爜',
    `warehouse_name` VARCHAR(255) DEFAULT NULL COMMENT '浠撳簱鍚嶇О',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '鍏ュ簱鎬婚噾棰濓紙涓嶅惈绋庯級',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `pay_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '瀹為檯浠樻閲戦',
    `in_status` TINYINT DEFAULT NULL COMMENT '鍏ュ簱鐘舵€侊細1-寰呭鏍?2-宸插鏍?3-宸插彇娑',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `audit_status` TINYINT DEFAULT NULL COMMENT '瀹℃牳鐘舵€侊細0-鏈鏍?1-宸插鏍?2-瀹℃牳椹冲洖',
    `audit_time` DATETIME DEFAULT NULL COMMENT '瀹℃牳鏃堕棿',
    `audit_by` BIGINT DEFAULT NULL COMMENT '瀹℃牳浜篒D',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_in_no` (`in_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_supplier_code` (`supplier_code`),
    KEY `idx_warehouse_code` (`warehouse_code`),
    KEY `idx_in_status` (`in_status`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閲囪喘鍏ュ簱涓昏〃';

CREATE TABLE IF NOT EXISTS `purchase_in_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `in_id` BIGINT NOT NULL COMMENT '鍏ュ簱鍗曚富琛↖D',
    `in_no` VARCHAR(64) NOT NULL COMMENT '鍏ュ簱鍗曠紪鍙',
    `order_id` BIGINT DEFAULT NULL COMMENT '鍏宠仈閲囪喘璁㈠崟ID',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈閲囪喘璁㈠崟缂栧彿',
    `item_no` INT DEFAULT NULL COMMENT '琛屽彿',
    `goods_id` BIGINT DEFAULT NULL COMMENT '鍟嗗搧ID',
    `goods_code` VARCHAR(64) DEFAULT NULL COMMENT '鍟嗗搧缂栫爜',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'SKU名称',
    `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条码',
    `goods_name` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧鍚嶇О',
    `goods_spec` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧瑙勬牸鍨嬪彿',
    `unit_code` VARCHAR(32) DEFAULT NULL COMMENT '璁￠噺鍗曚綅缂栫爜',
    `goods_unit` VARCHAR(32) DEFAULT NULL COMMENT '商品单位名称',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT '销售单位到库存基础单位的换算率',
    `order_quantity` DECIMAL(18,4) DEFAULT NULL COMMENT '璁㈠崟鏁伴噺',
    `in_quantity` DECIMAL(18,4) NOT NULL COMMENT '瀹為檯鍏ュ簱鏁伴噺',
    `unit_price` DECIMAL(18,4) NOT NULL COMMENT '鍗曚环锛堜笉鍚◣锛',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT '閲戦锛堜笉鍚◣锛',
    `tax_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '绋庣巼',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `batch_no` VARCHAR(64) DEFAULT NULL COMMENT '鎵规鍙',
    `expire_date` VARCHAR(20) DEFAULT NULL COMMENT '鏈夋晥鏈',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    KEY `idx_in_id` (`in_id`),
    KEY `idx_in_no` (`in_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_goods_code` (`goods_code`),
    KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閲囪喘鍏ュ簱鏄庣粏';

CREATE TABLE IF NOT EXISTS `purchase_return_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `return_no` VARCHAR(64) NOT NULL COMMENT '閫€璐у崟缂栧彿',
    `doc_name` VARCHAR(200) NOT NULL COMMENT '鍗曟嵁鍚嶇О锛堝繀濉紝榛樿锛氬崟鎹被鍨�+褰撳ぉ鏃ユ湡锛�',
    `in_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈鍏ュ簱鍗曠紪鍙',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联采购订单编号',
    `supplier_id` BIGINT DEFAULT NULL COMMENT '渚涘簲鍟咺D',
    `supplier_code` VARCHAR(64) DEFAULT NULL COMMENT '渚涘簲鍟嗙紪鐮',
    `supplier_name` VARCHAR(255) DEFAULT NULL COMMENT '渚涘簲鍟嗗悕绉',
    `return_date` VARCHAR(20) DEFAULT NULL COMMENT '閫€璐ф棩鏈',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT '浠撳簱缂栫爜',
    `warehouse_name` VARCHAR(255) DEFAULT NULL COMMENT '浠撳簱鍚嶇О',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '閫€璐ф€婚噾棰濓紙涓嶅惈绋庯級',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `pay_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '瀹為檯閫€娆鹃噾棰',
    `return_status` TINYINT DEFAULT NULL COMMENT '閫€璐х姸鎬侊細1-寰呭鏍?2-宸插鏍?3-宸插彇娑',
    `return_reason` VARCHAR(500) DEFAULT NULL COMMENT '閫€璐у師鍥',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `audit_status` TINYINT DEFAULT NULL COMMENT '瀹℃牳鐘舵€侊細0-鏈鏍?1-宸插鏍?2-瀹℃牳椹冲洖',
    `audit_time` DATETIME DEFAULT NULL COMMENT '瀹℃牳鏃堕棿',
    `audit_by` BIGINT DEFAULT NULL COMMENT '瀹℃牳浜篒D',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_return_no` (`return_no`),
    KEY `idx_in_no` (`in_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_supplier_code` (`supplier_code`),
    KEY `idx_return_status` (`return_status`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閲囪喘閫€璐т富琛';

CREATE TABLE IF NOT EXISTS `purchase_return_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `return_id` BIGINT NOT NULL COMMENT '閫€璐у崟涓昏〃ID',
    `return_no` VARCHAR(64) NOT NULL COMMENT '閫€璐у崟缂栧彿',
    `in_id` BIGINT DEFAULT NULL COMMENT '鍏宠仈鍏ュ簱鍗旾D',
    `in_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈鍏ュ簱鍗曠紪鍙',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联采购订单编号',
    `item_no` INT DEFAULT NULL COMMENT '琛屽彿',
    `goods_id` BIGINT DEFAULT NULL COMMENT '鍟嗗搧ID',
    `goods_code` VARCHAR(64) DEFAULT NULL COMMENT '鍟嗗搧缂栫爜',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'SKU名称',
    `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条码',
    `goods_name` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧鍚嶇О',
    `goods_spec` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧瑙勬牸鍨嬪彿',
    `unit_code` VARCHAR(32) DEFAULT NULL COMMENT '璁￠噺鍗曚綅缂栫爜',
    `goods_unit` VARCHAR(32) DEFAULT NULL COMMENT '商品单位名称',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT '销售单位到库存基础单位的换算率',
    `in_quantity` DECIMAL(18,4) DEFAULT NULL COMMENT '鍏ュ簱鏁伴噺',
    `return_quantity` DECIMAL(18,4) NOT NULL COMMENT '閫€璐ф暟閲',
    `unit_price` DECIMAL(18,4) NOT NULL COMMENT '鍗曚环锛堜笉鍚◣锛',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT '閲戦锛堜笉鍚◣锛',
    `tax_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '绋庣巼',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `batch_no` VARCHAR(64) DEFAULT NULL COMMENT '鎵规鍙',
    `expire_date` VARCHAR(20) DEFAULT NULL COMMENT '鏈夋晥鏈',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    KEY `idx_return_id` (`return_id`),
    KEY `idx_return_no` (`return_no`),
    KEY `idx_in_no` (`in_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_goods_code` (`goods_code`),
    KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閲囪喘閫€璐ф槑缁';

CREATE TABLE IF NOT EXISTS `supplier` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `supplier_code` VARCHAR(64) NOT NULL COMMENT '渚涘簲鍟嗙紪鐮',
    `supplier_name` VARCHAR(255) NOT NULL COMMENT '渚涘簲鍟嗗叏绉',
    `short_name` VARCHAR(100) DEFAULT NULL COMMENT '渚涘簲鍟嗙畝绉',
    `contact_name` VARCHAR(100) DEFAULT NULL COMMENT '鑱旂郴浜哄鍚',
    `contact_phone` VARCHAR(50) DEFAULT NULL COMMENT '鑱旂郴浜虹數璇',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '鐢靛瓙閭',
    `address` VARCHAR(500) DEFAULT NULL COMMENT '璇︾粏鍦板潃',
    `province` VARCHAR(100) DEFAULT NULL COMMENT '鐪佷唤',
    `city` VARCHAR(100) DEFAULT NULL COMMENT '鍩庡競',
    `district` VARCHAR(100) DEFAULT NULL COMMENT '鍖哄幙',
    `zip_code` VARCHAR(20) DEFAULT NULL COMMENT '閭斂缂栫爜',
    `tax_no` VARCHAR(50) DEFAULT NULL COMMENT '绋庡姟鐧昏鍙',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '寮€鎴烽摱琛屽悕绉',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '閾惰璐﹀彿',
    `supplier_type` VARCHAR(20) DEFAULT NULL COMMENT '渚涘簲鍟嗙被鍨嬶細1-鏅€氫緵搴斿晢 2-鏍稿績渚涘簲鍟?3-鎴樼暐渚涘簲鍟',
    `industry` VARCHAR(100) DEFAULT NULL COMMENT '鎵€灞炶涓',
    `credit_level` VARCHAR(10) DEFAULT NULL COMMENT '淇＄敤绛夌骇锛欰/B/C/D',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_supplier_code` (`supplier_code`),
    KEY `idx_supplier_name` (`supplier_name`),
    KEY `idx_supplier_type` (`supplier_type`),
    KEY `idx_industry` (`industry`),
    KEY `idx_credit_level` (`credit_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渚涘簲鍟嗚〃';



-- ============================================
-- psi-sale schema.sql
-- 鏉ユ簮: \psi-sale\src\main\resources\schema.sql
-- ============================================

CREATE TABLE IF NOT EXISTS `sale_order_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '閿€鍞鍗曠紪鍙',
    `doc_name` VARCHAR(200) NOT NULL COMMENT '鍗曟嵁鍚嶇О锛堝繀濉紝榛樿锛氬崟鎹被鍨�+褰撳ぉ鏃ユ湡锛�',
    `customer_id` BIGINT DEFAULT NULL COMMENT '瀹㈡埛ID',
    `customer_code` VARCHAR(64) DEFAULT NULL COMMENT '瀹㈡埛缂栫爜',
    `customer_name` VARCHAR(255) DEFAULT NULL COMMENT '瀹㈡埛鍚嶇О',
    `order_date` VARCHAR(20) DEFAULT NULL COMMENT '璁㈠崟鏃ユ湡',
    `delivery_date` VARCHAR(20) DEFAULT NULL COMMENT '浜よ揣鏃ユ湡',
    `sale_type` TINYINT DEFAULT NULL COMMENT '閿€鍞被鍨嬶細1-鏅€氶攢鍞?2-鎵瑰彂 3-闆跺敭',
    `payment_type` TINYINT DEFAULT NULL COMMENT '浠樻鏂瑰紡锛?-鐜伴噾 2-鍒峰崱 3-璧婇攢',
    `currency_code` VARCHAR(10) DEFAULT NULL COMMENT '璐у竵缂栫爜',
    `exchange_rate` DECIMAL(12,4) DEFAULT NULL COMMENT '姹囩巼',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '璁㈠崟鎬婚噾棰濓紙涓嶅惈绋庯級',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `discount_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '鎶樻墸閲戦',
    `pay_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '瀹為檯鏀舵閲戦',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT '浠撳簱缂栫爜',
    `warehouse_name` VARCHAR(255) DEFAULT NULL COMMENT '浠撳簱鍚嶇О',
    `order_status` TINYINT DEFAULT 1 COMMENT '璁㈠崟鐘舵€侊細1-寰呭鎵?2-瀹℃牳閫氳繃 3-宸插叆搴?4-宸插彇娑?5-宸插畬鎴',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閿€鍞鍗曚富琛?';

CREATE TABLE IF NOT EXISTS `sale_order_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `order_id` BIGINT NOT NULL COMMENT '璁㈠崟涓昏〃ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '璁㈠崟缂栧彿',
    `item_no` INT DEFAULT NULL COMMENT '琛屽彿',
    `goods_id` BIGINT DEFAULT NULL COMMENT '鍟嗗搧ID',
    `goods_code` VARCHAR(64) DEFAULT NULL COMMENT '鍟嗗搧缂栫爜',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'SKU名称',
    `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条码',
    `goods_name` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧鍚嶇О',
    `goods_spec` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧瑙勬牸鍨嬪彿',
    `unit_code` VARCHAR(32) DEFAULT NULL COMMENT '璁￠噺鍗曚綅缂栫爜',
    `goods_unit` VARCHAR(32) DEFAULT NULL COMMENT '商品单位名称',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT '销售单位到库存基础单位的换算率',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '閿€鍞暟閲',
    `unit_price` DECIMAL(18,4) NOT NULL COMMENT '鍗曚环锛堜笉鍚◣锛',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT '閲戦锛堜笉鍚◣锛',
    `tax_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '绋庣巼',
    `is_tax_inclusive` TINYINT DEFAULT 0 COMMENT '标价是否含税(0:否 1:是)',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `discount_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '鎶樻墸鐜',
    `discount_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '鎶樻墸閲戦',
    `net_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '鍑€閲戦锛堝惈绋庯級',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_goods_code` (`goods_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閿€鍞鍗曟槑缁?';

CREATE TABLE IF NOT EXISTS `sale_out_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `out_no` VARCHAR(64) NOT NULL COMMENT '鍑哄簱鍗曠紪鍙',
    `doc_name` VARCHAR(200) NOT NULL COMMENT '鍗曟嵁鍚嶇О锛堝繀濉紝榛樿锛氬崟鎹被鍨�+褰撳ぉ鏃ユ湡锛�',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈閿€鍞鍗曠紪鍙',
    `customer_id` BIGINT DEFAULT NULL COMMENT '瀹㈡埛ID',
    `customer_code` VARCHAR(64) DEFAULT NULL COMMENT '瀹㈡埛缂栫爜',
    `customer_name` VARCHAR(255) DEFAULT NULL COMMENT '瀹㈡埛鍚嶇О',
    `out_date` VARCHAR(20) DEFAULT NULL COMMENT '鍑哄簱鏃ユ湡',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT '浠撳簱缂栫爜',
    `warehouse_name` VARCHAR(255) DEFAULT NULL COMMENT '浠撳簱鍚嶇О',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '鍑哄簱鎬婚噾棰濓紙涓嶅惈绋庯級',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_out_no` (`out_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_customer_code` (`customer_code`),
    KEY `idx_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閿€鍞嚭搴撲富琛?';

CREATE TABLE IF NOT EXISTS `sale_out_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `out_id` BIGINT NOT NULL COMMENT '鍑哄簱鍗曚富琛↖D',
    `out_no` VARCHAR(64) NOT NULL COMMENT '鍑哄簱鍗曠紪鍙',
    `order_id` BIGINT DEFAULT NULL COMMENT '鍏宠仈閿€鍞鍗旾D',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈閿€鍞鍗曠紪鍙',
    `item_no` INT DEFAULT NULL COMMENT '琛屽彿',
    `goods_id` BIGINT DEFAULT NULL COMMENT '鍟嗗搧ID',
    `goods_code` VARCHAR(64) DEFAULT NULL COMMENT '鍟嗗搧缂栫爜',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'SKU名称',
    `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条码',
    `goods_name` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧鍚嶇О',
    `goods_spec` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧瑙勬牸鍨嬪彿',
    `unit_code` VARCHAR(32) DEFAULT NULL COMMENT '璁￠噺鍗曚綅缂栫爜',
    `goods_unit` VARCHAR(32) DEFAULT NULL COMMENT '商品单位名称',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT '销售单位到库存基础单位的换算率',
    `order_quantity` DECIMAL(18,4) DEFAULT NULL COMMENT '璁㈠崟鏁伴噺',
    `out_quantity` DECIMAL(18,4) NOT NULL COMMENT '瀹為檯鍑哄簱鏁伴噺',
    `unit_price` DECIMAL(18,4) NOT NULL COMMENT '鍗曚环锛堜笉鍚◣锛',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT '閲戦锛堜笉鍚◣锛',
    `tax_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '绋庣巼',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `batch_no` VARCHAR(64) DEFAULT NULL COMMENT '鎵规鍙',
    `expire_date` VARCHAR(20) DEFAULT NULL COMMENT '鏈夋晥鏈',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    KEY `idx_out_id` (`out_id`),
    KEY `idx_out_no` (`out_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_goods_code` (`goods_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閿€鍞嚭搴撴槑缁?';

CREATE TABLE IF NOT EXISTS `sale_return_main` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `return_no` VARCHAR(64) NOT NULL COMMENT '閿€鍞€€璐у崟缂栧彿',
    `doc_name` VARCHAR(200) NOT NULL COMMENT '鍗曟嵁鍚嶇О锛堝繀濉紝榛樿锛氬崟鎹被鍨�+褰撳ぉ鏃ユ湡锛�',
    `out_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈鍑哄簱鍗曠紪鍙',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈閿€鍞鍗曠紪鍙',
    `customer_id` BIGINT DEFAULT NULL COMMENT '瀹㈡埛ID',
    `customer_code` VARCHAR(64) DEFAULT NULL COMMENT '瀹㈡埛缂栫爜',
    `customer_name` VARCHAR(255) DEFAULT NULL COMMENT '瀹㈡埛鍚嶇О',
    `return_date` VARCHAR(20) DEFAULT NULL COMMENT '閫€璐ф棩鏈',
    `warehouse_code` VARCHAR(64) DEFAULT NULL COMMENT '浠撳簱缂栫爜',
    `warehouse_name` VARCHAR(255) DEFAULT NULL COMMENT '浠撳簱鍚嶇О',
    `total_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '閫€璐ф€婚噾棰濓紙涓嶅惈绋庯級',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `pay_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '瀹為檯閫€娆鹃噾棰',
    `return_reason` VARCHAR(500) DEFAULT NULL COMMENT '閫€璐у師鍥',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_return_no` (`return_no`),
    KEY `idx_out_no` (`out_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閿€鍞€€璐т富琛?';

CREATE TABLE IF NOT EXISTS `sale_return_item` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `return_id` BIGINT NOT NULL COMMENT '閫€璐у崟涓昏〃ID',
    `return_no` VARCHAR(64) NOT NULL COMMENT '閫€璐у崟缂栧彿',
    `out_id` BIGINT DEFAULT NULL COMMENT '鍏宠仈鍑哄簱鍗旾D',
    `out_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈鍑哄簱鍗曠紪鍙',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈閿€鍞鍗曠紪鍙',
    `item_no` INT DEFAULT NULL COMMENT '琛屽彿',
    `goods_id` BIGINT DEFAULT NULL COMMENT '鍟嗗搧ID',
    `goods_code` VARCHAR(64) DEFAULT NULL COMMENT '鍟嗗搧缂栫爜',
    `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(255) DEFAULT NULL COMMENT 'SKU名称',
    `barcode` VARCHAR(64) DEFAULT NULL COMMENT '条码',
    `goods_name` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧鍚嶇О',
    `goods_spec` VARCHAR(255) DEFAULT NULL COMMENT '鍟嗗搧瑙勬牸鍨嬪彿',
    `unit_code` VARCHAR(32) DEFAULT NULL COMMENT '璁￠噺鍗曚綅缂栫爜',
    `goods_unit` VARCHAR(32) DEFAULT NULL COMMENT '商品单位名称',
    `conversion_rate` DECIMAL(18,4) DEFAULT 1.0000 COMMENT '销售单位到库存基础单位的换算率',
    `out_quantity` DECIMAL(18,4) DEFAULT NULL COMMENT '鍑哄簱鏁伴噺',
    `return_quantity` DECIMAL(18,4) NOT NULL COMMENT '閫€璐ф暟閲',
    `unit_price` DECIMAL(18,4) NOT NULL COMMENT '鍗曚环锛堜笉鍚◣锛',
    `amount` DECIMAL(18,4) DEFAULT NULL COMMENT '閲戦锛堜笉鍚◣锛',
    `tax_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '绋庣巼',
    `tax_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绋庨',
    `batch_no` VARCHAR(64) DEFAULT NULL COMMENT '鎵规鍙',
    `expire_date` VARCHAR(20) DEFAULT NULL COMMENT '鏈夋晥鏈',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    KEY `idx_return_id` (`return_id`),
    KEY `idx_return_no` (`return_no`),
    KEY `idx_out_no` (`out_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_goods_code` (`goods_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閿€鍞€€璐ф槑缁?';

CREATE TABLE IF NOT EXISTS `customer` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `customer_code` VARCHAR(64) NOT NULL COMMENT '瀹㈡埛缂栫爜',
    `customer_name` VARCHAR(255) NOT NULL COMMENT '瀹㈡埛鍏ㄧО',
    `short_name` VARCHAR(100) DEFAULT NULL COMMENT '瀹㈡埛绠€绉',
    `contact_name` VARCHAR(100) DEFAULT NULL COMMENT '鑱旂郴浜哄鍚',
    `contact_phone` VARCHAR(50) DEFAULT NULL COMMENT '鑱旂郴浜虹數璇',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '鐢靛瓙閭',
    `address` VARCHAR(500) DEFAULT NULL COMMENT '璇︾粏鍦板潃',
    `province` VARCHAR(100) DEFAULT NULL COMMENT '鐪佷唤',
    `city` VARCHAR(100) DEFAULT NULL COMMENT '鍩庡競',
    `district` VARCHAR(100) DEFAULT NULL COMMENT '鍖哄幙',
    `zip_code` VARCHAR(20) DEFAULT NULL COMMENT '閭斂缂栫爜',
    `tax_no` VARCHAR(50) DEFAULT NULL COMMENT '绋庡姟鐧昏鍙',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '寮€鎴烽摱琛屽悕绉',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '閾惰璐﹀彿',
    `customer_type` VARCHAR(20) DEFAULT NULL COMMENT '瀹㈡埛绫诲瀷锛?-鏅€氬鎴?2-浼氬憳 3-鎵瑰彂鍟',
    `customer_level` VARCHAR(10) DEFAULT NULL COMMENT '瀹㈡埛绛夌骇锛欰/B/C/D',
    `credit_limit` DECIMAL(18,4) DEFAULT NULL COMMENT '淇＄敤棰濆害',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_customer_code` (`customer_code`),
    KEY `idx_customer_name` (`customer_name`),
    KEY `idx_customer_type` (`customer_type`),
    KEY `idx_customer_level` (`customer_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹㈡埛琛?';

CREATE TABLE IF NOT EXISTS `customer_debt` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `customer_id` BIGINT NOT NULL COMMENT '瀹㈡埛ID',
    `customer_code` VARCHAR(64) DEFAULT NULL COMMENT '瀹㈡埛缂栫爜',
    `customer_name` VARCHAR(255) DEFAULT NULL COMMENT '瀹㈡埛鍚嶇О',
    `total_debt_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '绱娆犳閲戦',
    `paid_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '宸茶繕娆鹃噾棰',
    `pending_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '寰呰繕娆鹃噾棰濓紙娆犳浣欓锛',
    `credit_limit` DECIMAL(18,4) DEFAULT NULL COMMENT '淇＄敤棰濆害',
    `available_credit` DECIMAL(18,4) DEFAULT NULL COMMENT '鍙敤淇＄敤棰濆害',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_customer_id` (`customer_id`),
    KEY `idx_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹㈡埛娆犳姹囨€昏〃';

CREATE TABLE IF NOT EXISTS `customer_debt_detail` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `customer_id` BIGINT NOT NULL COMMENT '瀹㈡埛ID',
    `customer_code` VARCHAR(64) DEFAULT NULL COMMENT '瀹㈡埛缂栫爜',
    `customer_name` VARCHAR(255) DEFAULT NULL COMMENT '瀹㈡埛鍚嶇О',
    `bill_type` TINYINT DEFAULT NULL COMMENT '鍏宠仈鍗曟嵁绫诲瀷锛?-閿€鍞鍗?2-閿€鍞嚭搴',
    `bill_no` VARCHAR(64) DEFAULT NULL COMMENT '鍏宠仈鍗曟嵁缂栧彿',
    `debt_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '娆犳閲戦',
    `paid_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '宸茶繕閲戦',
    `pending_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '寰呰繕閲戦',
    `debt_date` VARCHAR(20) DEFAULT NULL COMMENT '娆犳鏃ユ湡',
    `due_date` VARCHAR(20) DEFAULT NULL COMMENT '鍒版湡鏃ユ湡',
    `status` TINYINT DEFAULT NULL COMMENT '鐘舵€侊細1-寰呰繕娆?2-閮ㄥ垎杩樻 3-宸茬粨娓',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_customer_code` (`customer_code`),
    KEY `idx_bill_no` (`bill_no`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹㈡埛娆犳鏄庣粏琛?';

CREATE TABLE IF NOT EXISTS `customer_payment` (
    `id` BIGINT AUTO_INCREMENT COMMENT '涓婚敭ID',
    `data_uuid` VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '绉熸埛ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '杩樻鍗曠紪鍙',
    `customer_id` BIGINT NOT NULL COMMENT '瀹㈡埛ID',
    `customer_code` VARCHAR(64) DEFAULT NULL COMMENT '瀹㈡埛缂栫爜',
    `customer_name` VARCHAR(255) DEFAULT NULL COMMENT '瀹㈡埛鍚嶇О',
    `payment_date` VARCHAR(20) DEFAULT NULL COMMENT '杩樻鏃ユ湡',
    `payment_amount` DECIMAL(18,4) DEFAULT NULL COMMENT '杩樻閲戦',
    `payment_method` TINYINT DEFAULT NULL COMMENT '杩樻鏂瑰紡锛?-鐜伴噾 2-閾惰杞处 3-鏀粯瀹?4-寰俊 5-鍏朵粬',
    `account_no` VARCHAR(100) DEFAULT NULL COMMENT '鏀舵璐︽埛',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '鏀舵閾惰',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '澶囨敞',
    `create_by` BIGINT DEFAULT NULL COMMENT '鍒涘缓浜篒D',
    `create_time` DATETIME DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    `update_by` BIGINT DEFAULT NULL COMMENT '鏇存柊浜篒D',
    `update_time` DATETIME DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    `del_flag` TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪锛?-鏈垹闄?1-宸插垹闄',
    `status` TINYINT DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤 0-绂佺敤',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_customer_code` (`customer_code`),
    KEY `idx_payment_method` (`payment_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='瀹㈡埛杩樻琛?';

-- 同步日志表：记录客户数据最后同步时间，用于增量同步
CREATE TABLE IF NOT EXISTS `sync_log` (
    `data_uuid`           VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）',
    `tenant_id`           VARCHAR(50) NOT NULL,
    `type`                VARCHAR(10) NOT NULL,
    `last_download_time`  VARCHAR(20) DEFAULT NULL COMMENT '最后同步时间',
    PRIMARY KEY (`tenant_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步日志表';




-- ============================================
-- psi-system - V2__Create_System_Tables.sql
-- 鏉ユ簮: \psi-system\src\main\resources\db\migration\V2__Create_System_Tables.sql
-- ============================================


-- 租户表
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '租户ID',
    tenant_name VARCHAR(100) NOT NULL COMMENT '租户名称',
    tenant_code VARCHAR(50) NOT NULL UNIQUE COMMENT '租户编码',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(255) COMMENT '地址',
    expire_time DATETIME COMMENT '租户到期时间',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_tenant_code (tenant_code),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    dept_id BIGINT COMMENT '部门ID',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_dept_id (dept_id),
    INDEX idx_username (username),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_role_code (role_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 商铺表
CREATE TABLE IF NOT EXISTS shop_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商铺ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    shop_name VARCHAR(100) NOT NULL COMMENT '商铺名称',
    shop_code VARCHAR(50) NOT NULL UNIQUE COMMENT '商铺编码',
    address VARCHAR(255) COMMENT '商铺地址',
    phone VARCHAR(20) COMMENT '联系电话',
    manager VARCHAR(50) COMMENT '负责人',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_shop_code (shop_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商铺表';

-- 仓库表
CREATE TABLE IF NOT EXISTS warehouse_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '仓库ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    shop_id BIGINT NOT NULL COMMENT '商铺ID',
    warehouse_name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    warehouse_code VARCHAR(50) NOT NULL UNIQUE COMMENT '仓库编码',
    address VARCHAR(255) COMMENT '仓库地址',
    capacity DECIMAL(18,2) COMMENT '仓库容量',
    manager VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_warehouse_code (warehouse_code),
    INDEX idx_shop_id (shop_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    shop_id BIGINT DEFAULT 0 COMMENT '商铺ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_code VARCHAR(50) COMMENT '部门编码',
    leader VARCHAR(50) COMMENT '部门负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_parent_id (parent_id),
    INDEX idx_dept_code (dept_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典类型ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    dict_code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典编码',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    description VARCHAR(255) COMMENT '字典描述',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_dict_code (dict_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典数据ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
    dict_label VARCHAR(200) NOT NULL COMMENT '字典标签',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_dict_code (dict_code),
    INDEX idx_dict_value (dict_value),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(100) COMMENT '菜单编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    path VARCHAR(255) COMMENT '路由路径',
    component VARCHAR(255) COMMENT '组件路径',
    permission_code VARCHAR(100) COMMENT '权限编码',
    icon VARCHAR(100) COMMENT '菜单图标',
    menu_type TINYINT DEFAULT 0 COMMENT '菜单类型 0-目录 1-菜单 2-按钮',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_hidden TINYINT DEFAULT 0 COMMENT '是否隐藏 0-显示 1-隐藏',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_type (menu_type),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation_type VARCHAR(50) COMMENT '操作类型',
    module_name VARCHAR(100) COMMENT '模块名称',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    execution_time BIGINT COMMENT '执行时长(毫秒)',
    success TINYINT DEFAULT 1 COMMENT '是否成功 0-失败 1-成功',
    error_message TEXT COMMENT '错误信息',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_user_id (user_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_module_name (module_name),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    login_type VARCHAR(20) COMMENT '登录类型',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    success TINYINT DEFAULT 1 COMMENT '是否成功 0-失败 1-成功',
    error_message VARCHAR(500) COMMENT '错误信息',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';


-- ============================================
-- psi-system - V3__Init_Base_Data.sql
-- 鏉ユ簮: \psi-system\src\main\resources\db\migration\V3__Init_Base_Data.sql
-- ============================================

-- 初始化租户数据
INSERT INTO sys_tenant (id, tenant_name, tenant_code, contact_name, contact_phone, email, address, status, create_time, update_time)
VALUES (1, '默认租户', 'DEFAULT', '管理员', '13800138000', 'admin@erp.com', '北京市朝阳区', 1, NOW(), NOW());

-- 初始化商铺数据
INSERT INTO shop_info (id, shop_name, shop_code, address, phone, manager, tenant_id, status, create_time, update_time)
VALUES (1, '总部商铺', 'HQ_SHOP', '北京市朝阳区总部大厦', '13800138001', '管理员', 1, 1, NOW(), NOW());

-- 初始化仓库数据
INSERT INTO warehouse_info (id, warehouse_name, warehouse_code, address, phone, manager, shop_id, tenant_id, status, create_time, update_time)
VALUES (1, '总部仓库', 'HQ_WAREHOUSE', '北京市朝阳区仓库区', '13800138002', '管理员', 1, 1, 1, NOW(), NOW());

-- 初始化部门数据
INSERT INTO sys_dept (id, dept_name, dept_code, parent_id, leader, phone, sort_order, shop_id, tenant_id, status, create_time, update_time)
VALUES (1, '总部', 'HQ', 0, '管理员', '13800138000', 1, 1, 1, 1, NOW(), NOW());

-- 初始化角色数据
INSERT INTO sys_role (id, role_name, role_code, description, tenant_id, status, create_time, update_time)
VALUES 
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员', 1, 1, NOW(), NOW()),
(2, '管理员', 'ADMIN', '系统管理员', 1, 1, NOW(), NOW()),
(3, '普通用户', 'USER', '普通用户', 1, 1, NOW(), NOW());

-- 初始化菜单数据
INSERT INTO sys_menu (id, menu_name, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
VALUES 
(1, '系统管理', 0, '/system', '', '⚙️', 1, 1, 1, NOW(), NOW()),
(2, '用户管理', 1, '/users', 'views/UserManage.vue', '👤', 1, 1, 1, NOW(), NOW()),
(3, '角色管理', 1, '/roles', 'views/Role.vue', '🔒', 2, 1, 1, NOW(), NOW()),
(4, '部门管理', 1, '/depts', 'views/Dept.vue', '🏢', 3, 1, 1, NOW(), NOW()),
(5, '菜单管理', 1, '/menus', 'views/Menu.vue', '📋', 4, 1, 1, NOW(), NOW()),
(6, '字典类型', 1, '/dict-types', 'views/DictType.vue', '📚', 5, 1, 1, NOW(), NOW()),
(7, '字典数据', 1, '/dict-data', 'views/DictData.vue', '📝', 6, 1, 1, NOW(), NOW()),
(8, '商铺管理', 0, '/shops', 'views/Shop.vue', '🏪', 2, 1, 1, NOW(), NOW()),
(9, '仓库管理', 0, '/warehouses', 'views/Warehouse.vue', '📦', 3, 1, 1, NOW(), NOW()),
(10, '租户管理', 0, '/tenants', 'views/Tenant.vue', '🏢', 4, 1, 1, NOW(), NOW()),
(11, '仪表盘', 0, '/', 'views/Dashboard.vue', '📊', 0, 1, 1, NOW(), NOW());

-- 初始化字典类型数据
INSERT INTO sys_dict_type (id, dict_name, dict_code, description, tenant_id, status, create_time, update_time)
VALUES 
(1, '用户状态', 'user_status', '用户状态字典', 1, 1, NOW(), NOW()),
(2, '部门状态', 'dept_status', '部门状态字典', 1, 1, NOW(), NOW()),
(3, '角色状态', 'role_status', '角色状态字典', 1, 1, NOW(), NOW()),
(4, '菜单类型', 'menu_type', '菜单类型字典', 1, 1, NOW(), NOW());

-- 初始化字典数据
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
VALUES 
(1, 'user_status', '启用', '1', 1, 1, 1, NOW(), NOW()),
(2, 'user_status', '禁用', '0', 2, 1, 1, NOW(), NOW()),
(3, 'dept_status', '启用', '1', 1, 1, 1, NOW(), NOW()),
(4, 'dept_status', '禁用', '0', 2, 1, 1, NOW(), NOW()),
(5, 'role_status', '启用', '1', 1, 1, 1, NOW(), NOW()),
(6, 'role_status', '禁用', '0', 2, 1, 1, NOW(), NOW()),
(7, 'menu_type', '目录', '0', 1, 1, 1, NOW(), NOW()),
(8, 'menu_type', '菜单', '1', 2, 1, 1, NOW(), NOW()),
(9, 'menu_type', '按钮', '2', 3, 1, 1, NOW(), NOW());

-- 初始化用户数据 (密码为 MD5 加密的 Psi@2026#Admin)
INSERT INTO sys_user (id, username, password, nickname, email, phone, dept_id, tenant_id, status, create_time, update_time)
VALUES (1, 'admin', '76f200043474b6474795b42f39c03bd4', '超级管理员', 'admin@erp.com', '13800138009', 1, 1, 1, NOW(), NOW());

-- 初始化用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始化角色菜单关联
INSERT INTO sys_role_menu (role_id, menu_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11);


-- ============================================
-- psi-system - V4__Update_Dept_Shop_Id.sql
-- 鏉ユ簮: \psi-system\src\main\resources\db\migration\V4__Update_Dept_Shop_Id.sql
-- ============================================


-- 更新部门的 shop_id 字段
UPDATE sys_dept SET shop_id = 1 WHERE id IN (1, 2, 3);
UPDATE sys_dept SET shop_id = 2 WHERE id = 4;


-- ============================================
-- psi-system - V5__Add_Base_Data.sql
-- 鏉ユ簮: \psi-system\src\main\resources\db\migration\V5__Add_Base_Data.sql
-- ============================================

-- 添加租户数据
INSERT INTO sys_tenant (id, tenant_name, tenant_code, contact_name, contact_phone, email, address, status, create_time, update_time)
SELECT 1, '默认租户', 'DEFAULT', '管理员', '13800138000', 'admin@erp.com', '北京市朝阳区', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_tenant WHERE id = 1);

-- 添加商铺数据
INSERT INTO shop_info (id, shop_name, shop_code, address, phone, manager, tenant_id, status, create_time, update_time)
SELECT 1, '总部商铺', 'HQ_SHOP', '北京市朝阳区总部大厦', '13800138001', '管理员', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM shop_info WHERE id = 1);

-- 添加仓库数据
INSERT INTO warehouse_info (id, warehouse_name, warehouse_code, address, phone, manager, shop_id, tenant_id, status, create_time, update_time)
SELECT 1, '总部仓库', 'HQ_WAREHOUSE', '北京市朝阳区仓库区', '13800138002', '管理员', 1, 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM warehouse_info WHERE id = 1);

-- 添加角色数据
INSERT INTO sys_role (id, role_name, role_code, description, tenant_id, status, create_time, update_time)
SELECT 1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员', 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 1);
INSERT INTO sys_role (id, role_name, role_code, description, tenant_id, status, create_time, update_time)
SELECT 2, '管理员', 'ADMIN', '系统管理员', 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 2);
INSERT INTO sys_role (id, role_name, role_code, description, tenant_id, status, create_time, update_time)
SELECT 3, '普通用户', 'USER', '普通用户', 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 3);

-- 添加菜单数据
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 1, '系统管理', 'system', 0, '/system', '', '⚙️', 1, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 2, '用户管理', 'user', 1, '/users', 'views/UserManage.vue', '👤', 1, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 2);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 3, '角色管理', 'role', 1, '/roles', 'views/Role.vue', '🔒', 2, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 3);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 4, '部门管理', 'dept', 1, '/depts', 'views/Dept.vue', '🏢', 3, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 4);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 5, '菜单管理', 'menu', 1, '/menus', 'views/Menu.vue', '📋', 4, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 5);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 6, '字典类型', 'dictType', 1, '/dict-types', 'views/DictType.vue', '📚', 5, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 6);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 7, '字典数据', 'dictData', 1, '/dict-data', 'views/DictData.vue', '📝', 6, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 7);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 8, '商铺管理', 'shop', 0, '/shops', 'views/Shop.vue', '🏪', 2, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 8);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 9, '仓库管理', 'warehouse', 0, '/warehouses', 'views/Warehouse.vue', '📦', 3, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 10, '租户管理', 'tenant', 0, '/tenants', 'views/Tenant.vue', '🏢', 4, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 10);
INSERT INTO sys_menu (id, menu_name, menu_code, parent_id, path, component, icon, sort_order, tenant_id, status, create_time, update_time)
SELECT 11, '仪表盘', 'dashboard', 0, '/', 'views/Dashboard.vue', '📊', 0, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 11);

-- 添加字典类型数据
INSERT INTO sys_dict_type (id, dict_name, dict_code, description, tenant_id, status, create_time, update_time)
SELECT 1, '用户状态', 'user_status', '用户状态字典', 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE id = 1);
INSERT INTO sys_dict_type (id, dict_name, dict_code, description, tenant_id, status, create_time, update_time)
SELECT 2, '部门状态', 'dept_status', '部门状态字典', 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE id = 2);
INSERT INTO sys_dict_type (id, dict_name, dict_code, description, tenant_id, status, create_time, update_time)
SELECT 3, '角色状态', 'role_status', '角色状态字典', 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE id = 3);
INSERT INTO sys_dict_type (id, dict_name, dict_code, description, tenant_id, status, create_time, update_time)
SELECT 4, '菜单类型', 'menu_type', '菜单类型字典', 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE id = 4);

-- 添加字典数据
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 1, 'user_status', '启用', '1', 1, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 1);
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 2, 'user_status', '禁用', '0', 2, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 2);
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 3, 'dept_status', '启用', '1', 1, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 3);
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 4, 'dept_status', '禁用', '0', 2, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 4);
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 5, 'role_status', '启用', '1', 1, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 5);
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 6, 'role_status', '禁用', '0', 2, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 6);
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 7, 'menu_type', '目录', '0', 1, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 7);
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 8, 'menu_type', '菜单', '1', 2, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 8);
INSERT INTO sys_dict_data (id, dict_code, dict_label, dict_value, sort_order, tenant_id, status, create_time, update_time)
SELECT 9, 'menu_type', '按钮', '2', 3, 1, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE id = 9);

-- 添加用户角色关联
INSERT INTO sys_user_role (user_id, role_id)
SELECT 1, 1 WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 1 AND role_id = 1);

-- 添加角色菜单关联
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 1 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 3 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 3);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 4 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 4);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 5 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 5);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 6 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 6);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 7 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 7);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 8 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 8);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 9 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 9);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 10 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 10);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 11 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 11);


-- ============================================
-- psi-system - V6__Add_Description_Column.sql
-- 鏉ユ簮: \psi-system\src\main\resources\db\migration\V6__Add_Description_Column.sql
-- ============================================


-- 为部门表添加描述字段
ALTER TABLE sys_dept 
ADD COLUMN description VARCHAR(500) DEFAULT NULL COMMENT '描述' AFTER shop_id;


-- ============================================
-- psi-system - V7__Add_POS_Tables.sql
-- 鏉ユ簮: \psi-system\src\main\resources\db\migration\V7__Add_POS_Tables.sql
-- ============================================


-- =============================================
-- 收银机配置表（下行同步源表）
-- 后台管理收银机，数据通过 psi-sync 下行同步到 POS 机本地 SQLite
-- =============================================
CREATE TABLE IF NOT EXISTS `pos_config` (
    `id`            BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid`     VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）',
    `tenant_id`     BIGINT DEFAULT 0 COMMENT '租户ID',
    `pos_sn`        VARCHAR(100) NOT NULL COMMENT 'POS硬件序列号(全球唯一)',
    `shop_code`     VARCHAR(50) NOT NULL COMMENT '门店编码',
    `pos_id`        VARCHAR(50) NOT NULL COMMENT '收银机编号(如POS01)',
    `pos_name`      VARCHAR(100) DEFAULT NULL COMMENT '收银机名称',
    `create_by`     BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `status`        TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_pos_sn` (`pos_sn`),
    UNIQUE KEY `uk_shop_pos_id` (`shop_code`, `pos_id`),
    INDEX `idx_shop_code` (`shop_code`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_del_flag` (`del_flag`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收银机配置表';

-- =============================================
-- 收银员表（下行同步源表）
-- 后台管理收银员，数据通过 psi-sync 下行同步到 POS 机本地 SQLite
-- =============================================
CREATE TABLE IF NOT EXISTS `pos_operator` (
    `id`            BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `data_uuid`     VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）',
    `tenant_id`     BIGINT DEFAULT 0 COMMENT '租户ID',
    `shop_code`     VARCHAR(50) NOT NULL COMMENT '门店编码',
    `username`      VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password`      VARCHAR(255) NOT NULL COMMENT '登录密码',
    `real_name`     VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `role`          TINYINT DEFAULT 1 COMMENT '角色 1-收银员 2-管理员',
    `create_by`     BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    `status`        TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_uuid` (`data_uuid`),
    UNIQUE KEY `uk_shop_username` (`shop_code`, `username`),
    INDEX `idx_shop_code` (`shop_code`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_del_flag` (`del_flag`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收银员表';


-- ============================================
-- psi-message - V2__Create_Message_Tables.sql
-- 鏉ユ簮: \psi-message\src\main\resources\db\migration\V2__Create_Message_Tables.sql
-- ============================================


-- 消息表
CREATE TABLE IF NOT EXISTS `mq_message_record` (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
    message_id varchar(64) NOT NULL COMMENT '全局唯一消息ID，幂等主键',
    tenant_id varchar(32) DEFAULT '' COMMENT '租户ID',
    operator_id varchar(32) DEFAULT '' COMMENT '业务操作人ID',
    source_service varchar(64) NOT NULL COMMENT '生产者微服务名',
    exchange_name varchar(64) NOT NULL COMMENT '交换机名称',
    routing_key varchar(64) NOT NULL COMMENT '路由键',
    event_type varchar(32) NOT NULL COMMENT '业务事件类型',
    message_body text NOT NULL COMMENT '完整业务消息JSON体',
    ext_params varchar(1024) DEFAULT '' COMMENT '扩展参数JSON',
    msg_status tinyint NOT NULL DEFAULT '0' COMMENT '0待发送 1发送成功 2发送失败 3消费成功 4消费失败',
    send_time bigint NOT NULL COMMENT '消息发送时间戳',
    consume_time bigint DEFAULT NULL COMMENT '消息消费时间戳',
    error_msg varchar(2048) DEFAULT '' COMMENT '失败异常信息',
    create_by varchar(32) DEFAULT '' COMMENT '记录创建人ID',
    update_by varchar(32) DEFAULT '' COMMENT '记录更新人ID',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    data_uuid VARCHAR(64) DEFAULT '' COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id` (`message_id`),
    KEY `idx_msg_status` (`msg_status`),
    KEY `idx_tenant_service` (`tenant_id`,`source_service`),
    KEY `idx_data_uuid` (`data_uuid`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ消息落表记录表';
-- 死信表
CREATE TABLE IF NOT EXISTS msg_dead_letter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '死信ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    message_id VARCHAR(64) NOT NULL COMMENT '原始消息ID',
    original_topic VARCHAR(255) NOT NULL COMMENT '原始主题',
    content TEXT NOT NULL COMMENT '消息内容(JSON格式)',
    sender VARCHAR(100) COMMENT '发送者',
    receiver VARCHAR(100) COMMENT '接收者',
    reason VARCHAR(500) COMMENT '进入死信原因',
    error_message TEXT COMMENT '错误信息',
    failed_count INT DEFAULT 0 COMMENT '失败次数',
    last_failed_time DATETIME COMMENT '最后失败时间',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_message_id (message_id),
    INDEX idx_original_topic (original_topic),
    INDEX idx_receiver (receiver),
    INDEX idx_last_failed_time (last_failed_time),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status),
    data_uuid VARCHAR(64) DEFAULT '' COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    INDEX idx_data_uuid (data_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='死信表';

-- 死信待办表
CREATE TABLE IF NOT EXISTS msg_dead_letter_todo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '待办ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    dead_letter_id BIGINT NOT NULL COMMENT '死信ID',
    message_id VARCHAR(64) NOT NULL COMMENT '原始消息ID',
    handler VARCHAR(100) COMMENT '处理人',
    process_status TINYINT DEFAULT 0 COMMENT '处理状态 0-待处理 1-处理中 2-已完成 3-忽略',
    handle_type TINYINT DEFAULT 0 COMMENT '处理方式 0-重试 1-手动处理 2-丢弃',
    remark VARCHAR(500) COMMENT '处理备注',
    handle_time DATETIME COMMENT '处理时间',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除标识 0-未删除 1-已删除',
    status TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    INDEX idx_dead_letter_id (dead_letter_id),
    INDEX idx_message_id (message_id),
    INDEX idx_process_status (process_status),
    INDEX idx_handler (handler),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_del_flag (del_flag),
    INDEX idx_status (status),
    data_uuid VARCHAR(64) DEFAULT '' COMMENT '数据唯一标识（雪花算法生成），用于分布式数据同步',
    INDEX idx_data_uuid (data_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='死信待办表';


-- ============================================
-- psi-message - V3__Add_Retryable_Column.sql
-- 鏉ユ簮: \psi-message\src\main\resources\db\migration\V3__Add_Retryable_Column.sql
-- ============================================


-- 为死信表添加可重试字段
ALTER TABLE msg_dead_letter 
ADD COLUMN retryable TINYINT DEFAULT 1 COMMENT '是否可重试 0-不可重试 1-可重试' AFTER last_failed_time;

-- 添加索引
CREATE INDEX idx_retryable ON msg_dead_letter(retryable);


-- ============================================
-- psi-message - V4__Add_Next_Retry_Time_Column.sql
-- 鏉ユ簮: \psi-message\src\main\resources\db\migration\V4__Add_Next_Retry_Time_Column.sql
-- ============================================


-- 为死信表添加下次可重试时间字段，支持指数退避
ALTER TABLE msg_dead_letter
    ADD COLUMN next_retry_time DATETIME DEFAULT NULL COMMENT '下次可重试时间' AFTER retryable;

-- 添加索引，用于定时重试任务扫描
CREATE INDEX idx_next_retry_time ON msg_dead_letter(next_retry_time);



-- ============================================
-- psi-sale - V2__ADD_SELF_USE_OUT_TABLES.sql
-- 鏉ユ簮: \psi-sale\src\main\resources\DB\migration\V2__ADD_SELF_USE_OUT_TABLES.sql
-- ============================================

-- 自用出库正式表
CREATE TABLE IF NOT EXISTS sale_out_self_use_main (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据UUID',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    out_no VARCHAR(64) NOT NULL COMMENT '出库单编号',
    doc_name VARCHAR(200) NOT NULL DEFAULT '' COMMENT '单据名称',
    customer_id BIGINT DEFAULT NULL COMMENT '客户ID',
    customer_code VARCHAR(64) DEFAULT NULL COMMENT '客户编码',
    customer_name VARCHAR(255) DEFAULT NULL COMMENT '客户名称',
    out_date VARCHAR(20) DEFAULT NULL COMMENT '出库日期',
    warehouse_code VARCHAR(64) DEFAULT NULL COMMENT '仓库编码',
    warehouse_name VARCHAR(255) DEFAULT NULL COMMENT '仓库名称',
    total_amount DECIMAL(18,4) DEFAULT NULL COMMENT '出库总金额（不含税）',
    tax_amount DECIMAL(18,4) DEFAULT NULL COMMENT '税额',
    self_use_reason VARCHAR(500) DEFAULT NULL COMMENT '自用原因',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    del_flag TINYINT DEFAULT '0' COMMENT '删除标记(0:未删除 1:已删除)',
    status TINYINT DEFAULT '1' COMMENT '状态: 1-待审批 2-审核通过 3-已完成 4-已取消',
    PRIMARY KEY (id),
    UNIQUE KEY uk_out_no (out_no),
    UNIQUE KEY uk_data_uuid (data_uuid),
    KEY idx_customer_code (customer_code),
    KEY idx_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自用出库主表';

-- 自用出库明细表
CREATE TABLE IF NOT EXISTS sale_out_self_use_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据UUID',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    out_id BIGINT NOT NULL COMMENT '出库单主表ID',
    out_no VARCHAR(64) NOT NULL COMMENT '出库单编号',
    item_no INT DEFAULT NULL COMMENT '行号',
    goods_id BIGINT DEFAULT NULL COMMENT '商品ID',
    goods_code VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
    sku_code VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
    sku_name VARCHAR(255) DEFAULT NULL COMMENT 'SKU名称',
    goods_name VARCHAR(255) DEFAULT NULL COMMENT '商品名称',
    goods_spec VARCHAR(255) DEFAULT NULL COMMENT '商品规格型号',
    unit_code VARCHAR(32) DEFAULT NULL COMMENT '计量单位编码',
    conversion_rate DECIMAL(18,6) DEFAULT '1.000000' COMMENT '销售单位到库存基础单位的换算率',
    out_quantity DECIMAL(18,4) NOT NULL COMMENT '实际出库数量',
    unit_price DECIMAL(18,4) NOT NULL COMMENT '单价（不含税）',
    amount DECIMAL(18,4) DEFAULT NULL COMMENT '金额（不含税）',
    tax_rate DECIMAL(8,4) DEFAULT NULL COMMENT '税率',
    tax_amount DECIMAL(18,4) DEFAULT NULL COMMENT '税额',
    batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
    expire_date VARCHAR(20) DEFAULT NULL COMMENT '有效期',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    del_flag TINYINT DEFAULT '0' COMMENT '删除标记(0:未删除 1:已删除)',
    status TINYINT DEFAULT '1' COMMENT '状态: 1-待审批 2-审核通过 3-已完成 4-已取消',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_uuid (data_uuid),
    KEY idx_out_id (out_id),
    KEY idx_out_no (out_no),
    KEY idx_goods_code (goods_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自用出库明细表';



-- ============================================
-- psi-sale - V3__ADD_PROMOTION_TABLES.sql
-- 鏉ユ簮: \psi-sale\src\main\resources\DB\migration\V3__ADD_PROMOTION_TABLES.sql
-- ============================================

-- 促销活动表
CREATE TABLE IF NOT EXISTS promotion (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据UUID',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    promotion_no VARCHAR(64) NOT NULL COMMENT '促销活动编号',
    promotion_name VARCHAR(200) NOT NULL COMMENT '促销活动名称',
    promotion_type TINYINT DEFAULT 1 COMMENT '促销类型 1:满减 2:满件折扣 3:买赠',
    discount_type TINYINT DEFAULT 1 COMMENT '优惠类型 1:固定金额 2:折扣百分比',
    discount_value DECIMAL(18,4) DEFAULT NULL COMMENT '优惠值',
    min_amount DECIMAL(18,4) DEFAULT NULL COMMENT '最低消费金额',
    min_quantity DECIMAL(18,4) DEFAULT NULL COMMENT '最低购买数量',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    scope_type TINYINT DEFAULT 1 COMMENT '适用范围 1:全部商品 2:指定商品 3:指定分类',
    status TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    priority INT DEFAULT 0 COMMENT '优先级，数字越小优先级越高',
    superimposable TINYINT DEFAULT 0 COMMENT '是否可叠加 0:不可 1:可以',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT '0' COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_promotion_no (promotion_no),
    UNIQUE KEY uk_data_uuid (data_uuid),
    KEY idx_status (status),
    KEY idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销活动表';

-- 促销活动商品表
CREATE TABLE IF NOT EXISTS promotion_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据UUID',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    promotion_id BIGINT NOT NULL COMMENT '促销活动ID',
    promotion_no VARCHAR(64) NOT NULL COMMENT '促销活动编号',
    item_type TINYINT DEFAULT 1 COMMENT '类型 1:商品 2:分类',
    item_code VARCHAR(64) DEFAULT NULL COMMENT '商品/分类编码',
    item_name VARCHAR(200) DEFAULT NULL COMMENT '商品/分类名称',
    category_code VARCHAR(64) DEFAULT NULL COMMENT '分类编码',
    category_name VARCHAR(200) DEFAULT NULL COMMENT '分类名称',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT DEFAULT '0' COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_uuid (data_uuid),
    KEY idx_promotion_id (promotion_id),
    KEY idx_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销活动商品表';



-- ============================================
-- psi-sync - V1__INIT.sql
-- 鏉ユ簮: \psi-sync\src\main\resources\DB\migration\V1__INIT.sql
-- ============================================

-- 下行：进销存→POS
CREATE TABLE IF NOT EXISTS `down_sync` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `batch_uuid` VARCHAR(64) NOT NULL COMMENT '整批批次唯一UUID',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '租户ID',
    `shop_code` VARCHAR(50) COMMENT '商铺编码',
    `table_name` VARCHAR(50) NOT NULL COMMENT '目标业务表名',
    `json_data` TEXT NOT NULL COMMENT '批量明细JSON List',
    `sync_status` TINYINT DEFAULT 0 COMMENT '0待下载 1已下载',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入中间表自动生成时间(关键增量字段)',
    `last_download_time` DATETIME NULL COMMENT 'POS拉取完毕时间',
    UNIQUE INDEX `uk_batch_uuid` (`batch_uuid`) COMMENT '防同一批次重复插入',
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_shop_code` (`shop_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='下行同步中间表';

-- 上行：POS→进销存
CREATE TABLE IF NOT EXISTS `up_sync` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `batch_uuid` VARCHAR(64) NOT NULL COMMENT '批次唯一编号',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '租户ID',
    `shop_code` VARCHAR(50) COMMENT '商铺编码',
    `pos_sn` VARCHAR(100) NOT NULL COMMENT '收银机设备编码',
    `table_name` VARCHAR(50) NOT NULL COMMENT '单据对应表名',
    `json_data` TEXT NOT NULL COMMENT '单据集合JSON',
    `sync_status` TINYINT DEFAULT 0 COMMENT '0待处理 1成功 2失败',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '单据写入中间表时间',
    UNIQUE INDEX `uk_batch_uuid` (`batch_uuid`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_shop_code` (`shop_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上行单据中间表';


-- ============================================
-- psi-sync - V2__ALTER_UP_SYNC.sql
-- 鏉ユ簮: \psi-sync\src\main\resources\DB\migration\V2__ALTER_UP_SYNC.sql
-- ============================================

-- 修改上行同步表，允许 pos_sn 为空
ALTER TABLE `up_sync` 
    MODIFY COLUMN `pos_sn` VARCHAR(100) COMMENT '收银机设备编码';

-- 添加处理时间字段
ALTER TABLE `up_sync` 
    ADD COLUMN `process_time` DATETIME NULL COMMENT '处理时间';

-- 添加错误信息字段
ALTER TABLE `up_sync` 
    ADD COLUMN `error_msg` TEXT COMMENT '错误信息';

-- 添加重试次数字段
ALTER TABLE `up_sync` 
    ADD COLUMN `retry_count` INT DEFAULT 0 COMMENT '重试次数';

-- 更新索引
ALTER TABLE `up_sync` 
    ADD INDEX `idx_sync_status` (`sync_status`),
    ADD INDEX `idx_create_time` (`create_time`);


-- ============================================
-- psi-sync - V3__ADD_RECORD_ID.sql
-- 鏉ユ簮: \psi-sync\src\main\resources\DB\migration\V3__ADD_RECORD_ID.sql
-- ============================================

-- 添加 recordId 字段用于幂等性校验
ALTER TABLE `up_sync` ADD COLUMN `record_id` VARCHAR(64) COMMENT '记录唯一ID（用于幂等性校验）';

-- 创建 recordId 索引，提高查询性能
CREATE INDEX `idx_record_id` ON `up_sync` (`record_id`);


-- ============================================
-- psi-sync - V3__UP_SYNC_IDEMPOTENT.sql
-- 鏉ユ簮: \psi-sync\src\main\resources\DB\migration\V3__UP_SYNC_IDEMPOTENT.sql
-- ============================================

-- ========================================================
-- P0 优先级：上行同步表幂等性改造
-- 1. record_id 添加唯一索引（数据库级幂等约束）
-- 2. 添加 business_key 字段（业务主键，便于查询和冲突判断）
-- 3. 添加 data_version 字段（版本号冲突解决）
-- 4. 添加复合唯一索引（业务级幂等）
-- ========================================================

-- 1. 添加 business_key 字段
ALTER TABLE `up_sync`
    ADD COLUMN `business_key` VARCHAR(100) NULL COMMENT '业务主键（如订单号）' AFTER `table_name`;

-- 2. 添加 data_version 字段
ALTER TABLE `up_sync`
    ADD COLUMN `data_version` BIGINT DEFAULT 0 COMMENT '数据版本号（用于冲突解决）' AFTER `business_key`;

-- 3. 给已有数据的 record_id 补充唯一值（避免加唯一索引时冲突）
-- 注意：如果已有重复 record_id，需要先清理。以下脚本为历史数据生成确定性 record_id
UPDATE `up_sync`
SET `record_id` = CONCAT(COALESCE(`tenant_id`, 'default'), ':', `table_name`, ':', `batch_uuid`),
    `business_key` = `batch_uuid`
WHERE `record_id` IS NULL OR `record_id` = '';

-- 4. 添加 record_id 唯一索引（核心幂等约束）
ALTER TABLE `up_sync`
    ADD UNIQUE INDEX `uk_record_id` (`record_id`);

-- 5. 添加业务级复合唯一索引（同一业务数据只允许一条待处理记录）
ALTER TABLE `up_sync`
    ADD UNIQUE INDEX `uk_biz_pending` (`tenant_id`, `table_name`, `business_key`, `sync_status`);

-- 6. 添加业务键查询索引
ALTER TABLE `up_sync`
    ADD INDEX `idx_business_key` (`business_key`);



-- ============================================
-- psi-sync - V4__DOWN_SYNC_IDEMPOTENT.sql
-- 鏉ユ簮: \psi-sync\src\main\resources\DB\migration\V4__DOWN_SYNC_IDEMPOTENT.sql
-- ============================================

-- ========================================================
-- P1 优先级：下行同步表幂等性改造
-- 1. 添加 data_uuid 字段（业务数据唯一标识）
-- 2. 添加 data_version 字段（版本号冲突解决）
-- 3. 添加幂等索引（同一数据只保留最新版本）
-- ========================================================

-- 1. 添加 data_uuid 字段
ALTER TABLE `down_sync`
    ADD COLUMN `data_uuid` VARCHAR(64) NULL COMMENT '业务数据UUID' AFTER `table_name`;

-- 2. 添加 data_version 字段
ALTER TABLE `down_sync`
    ADD COLUMN `data_version` BIGINT DEFAULT 0 COMMENT '数据版本号' AFTER `data_uuid`;

-- 3. 为历史数据生成 data_uuid（基于 batch_uuid 兜底）
UPDATE `down_sync`
SET `data_uuid` = `batch_uuid`,
    `data_version` = 0
WHERE `data_uuid` IS NULL;

-- 4. 添加数据查询索引
ALTER TABLE `down_sync`
    ADD INDEX `idx_data_uuid` (`data_uuid`);

-- 5. 添加版本号查询索引
ALTER TABLE `down_sync`
    ADD INDEX `idx_data_version` (`data_version`);


-- ============================================================
-- PSI Customer Growth Module - customer_touchpoint
-- ============================================================

CREATE TABLE IF NOT EXISTS `customer_touchpoint` (
    `id`              INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `tenant_id`       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    `member_id`       BIGINT        DEFAULT NULL COMMENT '会员ID（关联 member_info.id）',
    `customer_id`     BIGINT        DEFAULT NULL COMMENT '客户ID（关联 customer.id，B2B大客户用）',
    `touchpoint_type` VARCHAR(32)   NOT NULL COMMENT '触点类型：INQUIRY / NEGOTIATION / WHATSAPP_MSG / STORE_VISIT / PHONE_CALL / SOCIAL / OTHER',
    `channel`         VARCHAR(32)   DEFAULT NULL COMMENT '渠道：WHATSAPP / PHONE / IN_STORE / FACEBOOK / OTHER',
    `contact_time`    VARCHAR(32)   NOT NULL COMMENT '接触时间 yyyy-MM-dd HH:mm:ss',
    `summary`         VARCHAR(500)  DEFAULT NULL COMMENT '触点摘要',
    `intent`          VARCHAR(32)   DEFAULT NULL COMMENT '客户意图：BUY / COMPARE / COMPLAINT / INFO / CHITCHAT',
    `follow_up`       VARCHAR(500)  DEFAULT NULL COMMENT '跟进动作',
    `follow_up_done`  TINYINT       DEFAULT 0 COMMENT '跟进是否完成 0-未完成 1-已完成',
    `operator`        VARCHAR(64)   DEFAULT NULL COMMENT '记录人',
    `create_time`     VARCHAR(32)   DEFAULT NULL COMMENT '创建时间',
    `update_time`     VARCHAR(32)   DEFAULT NULL COMMENT '更新时间',
    `del_flag`        TINYINT       DEFAULT 0 COMMENT '删除标志 0-正常 1-已删除',
    INDEX `idx_member_touchpoint` (`member_id`, `touchpoint_type`),
    INDEX `idx_customer_touchpoint` (`customer_id`, `touchpoint_type`),
    INDEX `idx_contact_time` (`contact_time`),
    INDEX `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户旅程触点记录（非购买类）';


-- ============================================================
-- PSI Customer Growth Module - customer_journey_config (zero-code config)
-- ============================================================

CREATE TABLE IF NOT EXISTS `customer_journey_config` (
    `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `config_group`  VARCHAR(50)  NOT NULL              COMMENT 'CHURN_MODEL / MESSAGE_TEMPLATE / TOUCHPOINT_TYPE / CHANNEL_RULE / SUGGESTED_ACTION / DASHBOARD',
    `config_key`    VARCHAR(100) NOT NULL              COMMENT 'Config key within group',
    `config_value`  TEXT                               COMMENT 'Config value',
    `value_type`    VARCHAR(20)  DEFAULT 'STRING'      COMMENT 'NUMBER / STRING / BOOLEAN / JSON / OPTION',
    `display_name`  VARCHAR(100)                       COMMENT 'Form label for admin UI',
    `description`   VARCHAR(255)                       COMMENT 'Help text',
    `sort_order`    INT          DEFAULT 0             COMMENT 'Display order within group',
    `del_flag`      INT          DEFAULT 0             COMMENT '0=active 1=deleted',
    `create_time`   VARCHAR(20)                        COMMENT 'yyyy-MM-dd HH:mm:ss',
    `update_time`   VARCHAR(20)                        COMMENT 'yyyy-MM-dd HH:mm:ss',
    UNIQUE KEY `uk_group_key` (`config_group`, `config_key`),
    INDEX `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Customer journey zero-code configuration';

-- Seed: CHURN_MODEL (7 items)
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('CHURN_MODEL', 'active_multiplier',     '1.5', 'NUMBER', 'Active threshold',     'Within this x normal cycle = ACTIVE',               1, 0, NOW(), NOW()),
('CHURN_MODEL', 'at_risk_multiplier',    '2.5', 'NUMBER', 'At-risk threshold',   'Within this x normal cycle = AT_RISK',              2, 0, NOW(), NOW()),
('CHURN_MODEL', 'high_risk_multiplier',  '4.0', 'NUMBER', 'High-risk threshold',  'Within this x normal cycle = HIGH_RISK',            3, 0, NOW(), NOW()),
('CHURN_MODEL', 'max_days_churned',      '90',  'NUMBER', 'Max days to churn',    'Absolute days before CHURNED regardless of cycle', 4, 0, NOW(), NOW()),
('CHURN_MODEL', 'new_customer_days',     '30',  'NUMBER', 'New customer days',    'Days below which a customer with <2 orders is NEW', 5, 0, NOW(), NOW()),
('CHURN_MODEL', 'fallback_active_days', '30',  'NUMBER', 'Fallback active days', 'Days for ACTIVE when no purchase history exists',   6, 0, NOW(), NOW()),
('CHURN_MODEL', 'fallback_at_risk_days', '60',  'NUMBER', 'Fallback at-risk days', 'Days for AT_RISK when no purchase history exists', 7, 0, NOW(), NOW());

-- Seed: MESSAGE_TEMPLATE (4 items)
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('MESSAGE_TEMPLATE', 'template_at_risk',     'Hi {name}! We haven''t seen you in {days} days. Last time you bought {product}. Come back this week for 10% off! Reply STOP to opt out.', 'STRING', 'At-risk message',    'Sent to AT_RISK customers',     1, 0, NOW(), NOW()),
('MESSAGE_TEMPLATE', 'template_high_risk',   'Hi {name}! It''s been {days} days since your last visit. Is everything OK? We''d love to see you again. Special offer inside!',                  'STRING', 'High-risk message',  'Sent to HIGH_RISK customers',   2, 0, NOW(), NOW()),
('MESSAGE_TEMPLATE', 'template_churned',      'Hi {name}! We miss you! It''s been {days} days. Here''s a special 15% off just for you. Valid this week only.',                                          'STRING', 'Churned message',    'Sent to CHURNED customers',     3, 0, NOW(), NOW()),
('MESSAGE_TEMPLATE', 'template_new_churned', 'Hi {name}! How was your first purchase of {product}? We''d love your feedback. Come back for 10% off your next order!',                                 'STRING', 'New-churned message', 'Sent to NEW_CHURNED customers', 4, 0, NOW(), NOW());

-- Seed: TOUCHPOINT_TYPE (6 items)
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('TOUCHPOINT_TYPE', 'WHATSAPP_INQUIRY',   'WhatsApp price inquiry', 'OPTION', 'WhatsApp inquiry',  'Customer asked price on WhatsApp', 1, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'STORE_VISIT',        'Store visit',             'OPTION', 'Store visit',        'Customer visited the shop',        2, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'PHONE_CALL',         'Phone call',              'OPTION', 'Phone call',         'Customer called by phone',          3, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'DELIVERY_FEEDBACK',  'Delivery feedback',       'OPTION', 'Delivery feedback', 'Feedback after delivery',           4, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'COMPLAINT',          'Complaint',               'OPTION', 'Complaint',          'Customer complained',               5, 0, NOW(), NOW()),
('TOUCHPOINT_TYPE', 'REFERRAL',           'Referral',                'OPTION', 'Referral',           'Customer referred someone',         6, 0, NOW(), NOW());

-- Seed: CHANNEL_RULE (2 items)
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('CHANNEL_RULE', 'high_value_threshold', '5000',     'NUMBER', 'High-value threshold', 'Total spent above this = use PHONE channel',         1, 0, NOW(), NOW()),
('CHANNEL_RULE', 'default_channel',     'WHATSAPP', 'STRING', 'Default channel',      'Default win-back channel for normal customers',    2, 0, NOW(), NOW());

-- Seed: SUGGESTED_ACTION (4 items)
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('SUGGESTED_ACTION', 'action_at_risk',     'Send WhatsApp: We miss you! 10% off this week',                   'STRING', 'At-risk action',     'Suggested action for AT_RISK',     1, 0, NOW(), NOW()),
('SUGGESTED_ACTION', 'action_high_risk',   'Call directly: Haven''t seen you, everything OK?',                'STRING', 'High-risk action',   'Suggested action for HIGH_RISK',    2, 0, NOW(), NOW()),
('SUGGESTED_ACTION', 'action_churned',     'Last attempt: Special 15% off just for you',                      'STRING', 'Churned action',      'Suggested action for CHURNED',      3, 0, NOW(), NOW()),
('SUGGESTED_ACTION', 'action_new_churned', 'Follow up: How was your first purchase?',                         'STRING', 'New-churned action',  'Suggested action for NEW_CHURNED',  4, 0, NOW(), NOW());

-- Seed: DASHBOARD (4 items)
INSERT INTO customer_journey_config (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time) VALUES
('DASHBOARD', 'top_customers_limit',   '5',  'NUMBER', 'Top customers shown', 'How many top customers on dashboard',  1, 0, NOW(), NOW()),
('DASHBOARD', 'top_churn_alerts_limit','10', 'NUMBER', 'Churn alerts shown',  'How many churn alerts on dashboard',   2, 0, NOW(), NOW()),
('DASHBOARD', 'timeline_limit',        '20', 'NUMBER', 'Timeline items shown', 'Max timeline items per customer',      3, 0, NOW(), NOW()),
('DASHBOARD', 'winback_limit',         '20', 'NUMBER', 'Winback suggestions',  'Max win-back suggestions returned',   4, 0, NOW(), NOW());

-- 补建 init.sql 中缺失的库存与单据正式表
-- 用于修复 Docker 重建 MySQL 后 stock/doc_main/doc_item 表缺失问题

CREATE TABLE IF NOT EXISTS `stock` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` BIGINT NULL,
  `create_by` BIGINT NULL,
  `create_time` DATETIME NULL,
  `update_by` BIGINT NULL,
  `update_time` DATETIME NULL,
  `del_flag` INT DEFAULT 0,
  `status` INT DEFAULT 1,
  `data_uuid` VARCHAR(64) NULL,
  `warehouse_code` VARCHAR(64) NULL,
  `warehouse_name` VARCHAR(128) NULL,
  `goods_code` VARCHAR(64) NULL,
  `sku_code` VARCHAR(64) NULL,
  `goods_name` VARCHAR(256) NULL,
  `goods_spec` VARCHAR(256) NULL,
  `unit` VARCHAR(32) NULL,
  `quantity` DECIMAL(18,4) DEFAULT 0,
  `locked_quantity` DECIMAL(18,4) DEFAULT 0,
  `available_quantity` DECIMAL(18,4) DEFAULT 0,
  `avg_cost_price` DECIMAL(18,4) DEFAULT 0,
  `total_amount` DECIMAL(18,4) DEFAULT 0,
  UNIQUE KEY `uk_warehouse_sku` (`warehouse_code`, `sku_code`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `doc_main` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` BIGINT NULL,
  `create_by` BIGINT NULL,
  `create_time` DATETIME NULL,
  `update_by` BIGINT NULL,
  `update_time` DATETIME NULL,
  `del_flag` INT DEFAULT 0,
  `status` INT DEFAULT 1,
  `data_uuid` VARCHAR(64) NULL,
  `doc_no` VARCHAR(64) NULL,
  `doc_type` VARCHAR(32) NULL,
  `doc_name` VARCHAR(256) NULL,
  `shop_code` VARCHAR(64) NULL,
  `shop_name` VARCHAR(128) NULL,
  `dept_id` BIGINT NULL,
  `dept_name` VARCHAR(128) NULL,
  `partner_id` BIGINT NULL,
  `partner_code` VARCHAR(64) NULL,
  `partner_name` VARCHAR(128) NULL,
  `warehouse_id` BIGINT NULL,
  `warehouse_code` VARCHAR(64) NULL,
  `warehouse_name` VARCHAR(128) NULL,
  `order_no` VARCHAR(64) NULL,
  `sale_type` VARCHAR(32) NULL,
  `payment_type` VARCHAR(32) NULL,
  `currency_code` VARCHAR(16) NULL,
  `exchange_rate` DECIMAL(18,6) DEFAULT 1,
  `tax_amount` DECIMAL(18,4) DEFAULT 0,
  `discount_amount` DECIMAL(18,4) DEFAULT 0,
  `total_amount` DECIMAL(18,4) DEFAULT 0,
  `pay_amount` DECIMAL(18,4) DEFAULT 0,
  `item_count` INT DEFAULT 0,
  `doc_date` DATETIME NULL,
  `delivery_date` DATETIME NULL,
  `remark` VARCHAR(512) NULL,
  `ext_json` JSON NULL,
  KEY `idx_doc_no` (`doc_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `doc_item` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` BIGINT NULL,
  `create_by` BIGINT NULL,
  `create_time` DATETIME NULL,
  `update_by` BIGINT NULL,
  `update_time` DATETIME NULL,
  `del_flag` INT DEFAULT 0,
  `status` INT DEFAULT 1,
  `data_uuid` VARCHAR(64) NULL,
  `doc_id` BIGINT NULL,
  `doc_no` VARCHAR(64) NULL,
  `shop_code` VARCHAR(64) NULL,
  `shop_name` VARCHAR(128) NULL,
  `goods_id` BIGINT NULL,
  `goods_code` VARCHAR(64) NULL,
  `sku_code` VARCHAR(64) NULL,
  `sku_name` VARCHAR(256) NULL,
  `barcode` VARCHAR(64) NULL,
  `goods_name` VARCHAR(256) NULL,
  `goods_spec` VARCHAR(256) NULL,
  `unit_code` VARCHAR(32) NULL,
  `goods_unit` VARCHAR(32) NULL,
  `conversion_rate` DECIMAL(18,6) DEFAULT 1,
  `unit_price` DECIMAL(18,4) DEFAULT 0,
  `quantity` DECIMAL(18,4) DEFAULT 0,
  `tax_rate` DECIMAL(8,6) DEFAULT 0,
  `discount_rate` DECIMAL(8,6) DEFAULT 0,
  `discount_amount` DECIMAL(18,4) DEFAULT 0,
  `cost_price` DECIMAL(18,4) DEFAULT 0,
  `stock_id` BIGINT NULL,
  `batch_no` VARCHAR(64) NULL,
  `expiry_date` DATE NULL,
  `amount` DECIMAL(18,4) DEFAULT 0,
  `tax_amount` DECIMAL(18,4) DEFAULT 0,
  `remark` VARCHAR(512) NULL,
  `line_no` INT DEFAULT 0,
  KEY `idx_doc_id` (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

