-- psi-flow 工作流微服务 建表脚本
USE psi_flow;

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