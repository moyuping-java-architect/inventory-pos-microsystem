CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    role VARCHAR(20) DEFAULT 'USER' COMMENT 'ADMIN/USER/CASHIER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_name VARCHAR(100) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    menu_type TINYINT DEFAULT 1 COMMENT '1菜单 2按钮',
    permission VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

CREATE TABLE IF NOT EXISTS shop_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    phone VARCHAR(20),
    tax_id VARCHAR(50),
    currency VARCHAR(10) DEFAULT 'ZMW',
    exchange_rate DECIMAL(10,4) DEFAULT 1.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺信息表';

CREATE TABLE IF NOT EXISTS goods_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_code VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编码',
    goods_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT COMMENT '分类ID',
    brand VARCHAR(50),
    unit VARCHAR(20) COMMENT '单位',
    bar_code VARCHAR(100) COMMENT '条码',
    purchase_price DECIMAL(12,2) DEFAULT 0 COMMENT '进价',
    sale_price DECIMAL(12,2) DEFAULT 0 COMMENT '售价',
    member_price DECIMAL(12,2) DEFAULT 0 COMMENT '会员价',
    stock_qty DECIMAL(12,4) DEFAULT 0 COMMENT '库存数量',
    min_stock DECIMAL(12,4) DEFAULT 0 COMMENT '最低库存',
    max_stock DECIMAL(12,4) DEFAULT 99999 COMMENT '最高库存',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    image_url VARCHAR(500),
    description VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_goods_code (goods_code),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS goods_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL,
    sku_code VARCHAR(50) NOT NULL UNIQUE,
    sku_name VARCHAR(100),
    spec VARCHAR(200),
    stock_qty DECIMAL(12,4) DEFAULT 0,
    price DECIMAL(12,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    phone VARCHAR(20),
    address VARCHAR(200),
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    address VARCHAR(200),
    total_spent DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费',
    member_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT 'NORMAL/GOLD/DIAMOND',
    balance DECIMAL(12,2) DEFAULT 0 COMMENT '储值余额',
    points INT DEFAULT 0 COMMENT '积分',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户/会员表';

CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '采购单号',
    supplier_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/IN_STOCK/CANCELLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    FOREIGN KEY (supplier_id) REFERENCES supplier(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    sku_id BIGINT,
    qty DECIMAL(12,4) NOT NULL COMMENT '采购数量',
    price DECIMAL(12,2) NOT NULL COMMENT '单价',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    FOREIGN KEY (order_id) REFERENCES purchase_order(id) ON DELETE CASCADE,
    FOREIGN KEY (goods_id) REFERENCES goods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

CREATE TABLE IF NOT EXISTS sale_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '销售单号',
    customer_id BIGINT COMMENT '客户ID',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    discount_amount DECIMAL(12,2) DEFAULT 0 COMMENT '优惠金额',
    actual_amount DECIMAL(12,2) DEFAULT 0 COMMENT '实付金额',
    payment_type VARCHAR(20) DEFAULT 'CASH' COMMENT 'CASH/CARD/MOBILE/MEMBER_BALANCE',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/PAYED/COMPLETED/REFUNDED',
    remark VARCHAR(500),
    cashier_id BIGINT COMMENT '收银员ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单表';

CREATE TABLE IF NOT EXISTS sale_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    sku_id BIGINT,
    qty DECIMAL(12,4) NOT NULL COMMENT '销售数量',
    price DECIMAL(12,2) NOT NULL COMMENT '单价',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    FOREIGN KEY (order_id) REFERENCES sale_order(id) ON DELETE CASCADE,
    FOREIGN KEY (goods_id) REFERENCES goods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单明细表';

CREATE TABLE IF NOT EXISTS stock_flow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL,
    sku_id BIGINT,
    flow_type VARCHAR(20) COMMENT 'IN/OUT/TRANSFER/ADJUST',
    qty DECIMAL(12,4) NOT NULL,
    before_qty DECIMAL(12,4) NOT NULL,
    after_qty DECIMAL(12,4) NOT NULL,
    source_order_no VARCHAR(50),
    remark VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_goods_id (goods_id),
    FOREIGN KEY (goods_id) REFERENCES goods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

CREATE TABLE IF NOT EXISTS cashier_shift (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cashier_id BIGINT NOT NULL,
    shift_no VARCHAR(50) NOT NULL UNIQUE,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    start_cash DECIMAL(12,2) DEFAULT 0 COMMENT '起始现金',
    end_cash DECIMAL(12,2) DEFAULT 0 COMMENT '结束现金',
    total_sales DECIMAL(12,2) DEFAULT 0 COMMENT '总销售额',
    status VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN/CLOSED',
    remark VARCHAR(500),
    INDEX idx_shift_no (shift_no),
    FOREIGN KEY (cashier_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收银交接班表';

CREATE TABLE IF NOT EXISTS promotion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    promo_name VARCHAR(100) NOT NULL,
    promo_type VARCHAR(20) DEFAULT 'DISCOUNT' COMMENT 'DISCOUNT/FULL_REDUCE/BUNDLE',
    discount_rate DECIMAL(5,2) COMMENT '折扣率',
    full_amount DECIMAL(12,2) COMMENT '满减门槛',
    reduce_amount DECIMAL(12,2) COMMENT '满减金额',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    priority INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    goods_ids TEXT COMMENT '适用商品ID列表',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

CREATE TABLE IF NOT EXISTS refund_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    original_order_no VARCHAR(50) NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_original_order (original_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货单表';

INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '系统管理员', 'ADMIN', 1),
('cashier', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '收银员', 'CASHIER', 1);

INSERT INTO shop_info (shop_name, address, currency) VALUES
('TradeMaster Store', 'Lusaka, Zambia', 'ZMW');

-- ============================================================
-- 仓库与库存模块表结构
-- ============================================================

-- 仓库表
CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    warehouse_code VARCHAR(50) NOT NULL UNIQUE COMMENT '仓库编码',
    warehouse_name VARCHAR(200) NOT NULL COMMENT '仓库名称',
    address VARCHAR(500) COMMENT '仓库地址',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 库存表（按SKU+仓库+批次管理）
CREATE TABLE IF NOT EXISTS stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    warehouse_code VARCHAR(50) NOT NULL COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    goods_code VARCHAR(50) NOT NULL COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) NOT NULL COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit VARCHAR(20) COMMENT '单位',
    quantity DECIMAL(12,4) DEFAULT 0 COMMENT '实际库存数量',
    locked_quantity DECIMAL(12,4) DEFAULT 0 COMMENT '锁定库存数量',
    available_quantity DECIMAL(12,4) DEFAULT 0 COMMENT '可用库存数量',
    avg_cost_price DECIMAL(12,4) DEFAULT 0 COMMENT '平均成本价',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '库存总金额',
    batch_no VARCHAR(50) COMMENT '批次号',
    expiry_date DATE COMMENT '有效期',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-过期',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sku_warehouse_batch (warehouse_code, sku_code, batch_no),
    INDEX idx_warehouse_code (warehouse_code),
    INDEX idx_sku_code (sku_code),
    INDEX idx_goods_code (goods_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 库存流水表
CREATE TABLE IF NOT EXISTS stock_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    warehouse_code VARCHAR(50) NOT NULL COMMENT '仓库编码',
    goods_code VARCHAR(50) NOT NULL COMMENT '商品编码',
    sku_code VARCHAR(50) NOT NULL COMMENT 'SKU编码',
    flow_type VARCHAR(20) NOT NULL COMMENT '流水类型 IN-入库 OUT-出库 TRANSFER-调拨 LOSS-报损 OVERFLOW-报溢 CHECK-盘点',
    quantity DECIMAL(12,4) NOT NULL COMMENT '变动数量',
    before_quantity DECIMAL(12,4) NOT NULL COMMENT '变动前数量',
    after_quantity DECIMAL(12,4) NOT NULL COMMENT '变动后数量',
    avg_cost_price DECIMAL(12,4) DEFAULT 0 COMMENT '成本价',
    amount DECIMAL(14,4) DEFAULT 0 COMMENT '金额',
    source_no VARCHAR(50) COMMENT '来源单号',
    source_type VARCHAR(20) COMMENT '来源类型',
    remark VARCHAR(500) COMMENT '备注',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_warehouse_code (warehouse_code),
    INDEX idx_sku_code (sku_code),
    INDEX idx_source_no (source_no),
    INDEX idx_flow_type (flow_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ============================================================
-- 采购模块正式业务表
-- ============================================================

-- 采购订单表
CREATE TABLE IF NOT EXISTS purchase_order_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '采购单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    supplier_code VARCHAR(50) COMMENT '供应商编码',
    supplier_name VARCHAR(200) COMMENT '供应商名称',
    payment_type INTEGER DEFAULT 1 COMMENT '付款方式',
    currency_code VARCHAR(10) DEFAULT 'ZMW' COMMENT '币种',
    exchange_rate DECIMAL(10,4) DEFAULT 1.0 COMMENT '汇率',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    tax_amount DECIMAL(14,4) DEFAULT 0 COMMENT '税额',
    discount_amount DECIMAL(14,4) DEFAULT 0 COMMENT '折扣金额',
    pay_amount DECIMAL(14,4) DEFAULT 0 COMMENT '实付金额',
    order_date DATETIME COMMENT '下单日期',
    delivery_date DATETIME COMMENT '交货日期',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-执行中 3-已完成 -1-已取消',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人',
    audit_time DATETIME COMMENT '审核时间',
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单主表';

-- 采购订单明细表
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    conversion_rate DECIMAL(12,4) DEFAULT 1 COMMENT '换算率',
    quantity DECIMAL(12,4) NOT NULL COMMENT '数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    tax_rate DECIMAL(6,4) DEFAULT 0 COMMENT '税率',
    discount_rate DECIMAL(6,4) DEFAULT 0 COMMENT '折扣率',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (order_id) REFERENCES purchase_order_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

-- 采购入库单主表
CREATE TABLE IF NOT EXISTS purchase_in_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    in_no VARCHAR(50) NOT NULL UNIQUE COMMENT '入库单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    supplier_code VARCHAR(50) COMMENT '供应商编码',
    supplier_name VARCHAR(200) COMMENT '供应商名称',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    order_no VARCHAR(50) COMMENT '关联采购订单号',
    in_date DATETIME COMMENT '入库日期',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-已入库 3-已完成',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_in_no (in_no),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购入库单主表';

-- 采购入库明细表
CREATE TABLE IF NOT EXISTS purchase_in_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    in_id BIGINT NOT NULL COMMENT '入库单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    conversion_rate DECIMAL(12,4) DEFAULT 1 COMMENT '换算率',
    in_quantity DECIMAL(12,4) NOT NULL COMMENT '入库数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    tax_rate DECIMAL(6,4) DEFAULT 0 COMMENT '税率',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    batch_no VARCHAR(50) COMMENT '批次号',
    expire_date DATE COMMENT '有效期',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (in_id) REFERENCES purchase_in_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购入库明细表';

-- 采购退货单主表
CREATE TABLE IF NOT EXISTS purchase_return_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    return_no VARCHAR(50) NOT NULL UNIQUE COMMENT '退货单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    supplier_code VARCHAR(50) COMMENT '供应商编码',
    supplier_name VARCHAR(200) COMMENT '供应商名称',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    order_no VARCHAR(50) COMMENT '关联采购订单号',
    return_date DATETIME COMMENT '退货日期',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    return_reason VARCHAR(500) COMMENT '退货原因',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-已退货 3-已完成',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_return_no (return_no),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货单主表';

-- 采购退货明细表
CREATE TABLE IF NOT EXISTS purchase_return_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    return_id BIGINT NOT NULL COMMENT '退货单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    conversion_rate DECIMAL(12,4) DEFAULT 1 COMMENT '换算率',
    return_quantity DECIMAL(12,4) NOT NULL COMMENT '退货数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    tax_rate DECIMAL(6,4) DEFAULT 0 COMMENT '税率',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    batch_no VARCHAR(50) COMMENT '批次号',
    expire_date DATE COMMENT '有效期',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (return_id) REFERENCES purchase_return_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货明细表';

-- ============================================================
-- 销售模块正式业务表
-- ============================================================

-- 销售订单主表
CREATE TABLE IF NOT EXISTS sale_order_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '销售单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    customer_id BIGINT COMMENT '客户ID',
    customer_code VARCHAR(50) COMMENT '客户编码',
    customer_name VARCHAR(200) COMMENT '客户名称',
    sale_type INTEGER DEFAULT 1 COMMENT '销售类型 1-普通销售 2-批发 3-零售',
    payment_type INTEGER DEFAULT 1 COMMENT '付款方式',
    currency_code VARCHAR(10) DEFAULT 'ZMW' COMMENT '币种',
    exchange_rate DECIMAL(10,4) DEFAULT 1.0 COMMENT '汇率',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    tax_amount DECIMAL(14,4) DEFAULT 0 COMMENT '税额',
    discount_amount DECIMAL(14,4) DEFAULT 0 COMMENT '折扣金额',
    pay_amount DECIMAL(14,4) DEFAULT 0 COMMENT '实付金额',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-执行中 3-已完成 -1-已取消',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单主表';

-- 销售订单明细表
CREATE TABLE IF NOT EXISTS sale_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    conversion_rate DECIMAL(12,4) DEFAULT 1 COMMENT '换算率',
    quantity DECIMAL(12,4) NOT NULL COMMENT '数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    tax_rate DECIMAL(6,4) DEFAULT 0 COMMENT '税率',
    discount_rate DECIMAL(6,4) DEFAULT 0 COMMENT '折扣率',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (order_id) REFERENCES sale_order_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单明细表';

-- 销售出库单主表
CREATE TABLE IF NOT EXISTS sale_out_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    out_no VARCHAR(50) NOT NULL UNIQUE COMMENT '出库单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    customer_code VARCHAR(50) COMMENT '客户编码',
    customer_name VARCHAR(200) COMMENT '客户名称',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    order_no VARCHAR(50) COMMENT '关联销售订单号',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-已出库 3-已完成',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_out_no (out_no),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售出库单主表';

-- 销售出库明细表
CREATE TABLE IF NOT EXISTS sale_out_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    out_id BIGINT NOT NULL COMMENT '出库单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    conversion_rate DECIMAL(12,4) DEFAULT 1 COMMENT '换算率',
    out_quantity DECIMAL(12,4) NOT NULL COMMENT '出库数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    tax_rate DECIMAL(6,4) DEFAULT 0 COMMENT '税率',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    order_no VARCHAR(50) COMMENT '关联销售订单号',
    batch_no VARCHAR(50) COMMENT '批次号',
    expire_date DATE COMMENT '有效期',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (out_id) REFERENCES sale_out_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售出库明细表';

-- 销售退货单主表
CREATE TABLE IF NOT EXISTS sale_return_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    return_no VARCHAR(50) NOT NULL UNIQUE COMMENT '退货单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    customer_code VARCHAR(50) COMMENT '客户编码',
    customer_name VARCHAR(200) COMMENT '客户名称',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    order_no VARCHAR(50) COMMENT '关联销售订单号',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-已退货 3-已完成',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_return_no (return_no),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售退货单主表';

-- 销售退货明细表
CREATE TABLE IF NOT EXISTS sale_return_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    return_id BIGINT NOT NULL COMMENT '退货单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    conversion_rate DECIMAL(12,4) DEFAULT 1 COMMENT '换算率',
    return_quantity DECIMAL(12,4) NOT NULL COMMENT '退货数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    tax_rate DECIMAL(6,4) DEFAULT 0 COMMENT '税率',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    order_no VARCHAR(50) COMMENT '关联销售订单号',
    batch_no VARCHAR(50) COMMENT '批次号',
    expire_date DATE COMMENT '有效期',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (return_id) REFERENCES sale_return_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售退货明细表';

-- ============================================================
-- 库存调整模块正式业务表
-- ============================================================

-- 报损单主表
CREATE TABLE IF NOT EXISTS stock_loss_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    loss_no VARCHAR(50) NOT NULL UNIQUE COMMENT '报损单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    loss_reason VARCHAR(500) COMMENT '报损原因',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-已处理 3-已完成',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loss_no (loss_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损单主表';

-- 报损明细表
CREATE TABLE IF NOT EXISTS stock_loss_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    loss_id BIGINT NOT NULL COMMENT '报损单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    loss_quantity DECIMAL(12,4) NOT NULL COMMENT '报损数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    tax_rate DECIMAL(6,4) DEFAULT 0 COMMENT '税率',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    batch_no VARCHAR(50) COMMENT '批次号',
    expire_date DATE COMMENT '有效期',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (loss_id) REFERENCES stock_loss_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损明细表';

-- 报溢单主表
CREATE TABLE IF NOT EXISTS stock_overflow_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    overflow_no VARCHAR(50) NOT NULL UNIQUE COMMENT '报溢单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    overflow_reason VARCHAR(500) COMMENT '报溢原因',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-已处理 3-已完成',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_overflow_no (overflow_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报溢单主表';

-- 报溢明细表
CREATE TABLE IF NOT EXISTS stock_overflow_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    overflow_id BIGINT NOT NULL COMMENT '报溢单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    overflow_quantity DECIMAL(12,4) NOT NULL COMMENT '报溢数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    tax_rate DECIMAL(6,4) DEFAULT 0 COMMENT '税率',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    batch_no VARCHAR(50) COMMENT '批次号',
    expire_date DATE COMMENT '有效期',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (overflow_id) REFERENCES stock_overflow_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报溢明细表';

-- 盘点单主表
CREATE TABLE IF NOT EXISTS stock_check_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    check_no VARCHAR(50) NOT NULL UNIQUE COMMENT '盘点单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(200) COMMENT '仓库名称',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    difference_amount DECIMAL(14,4) DEFAULT 0 COMMENT '差异金额',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-已盘点 3-已完成',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_check_no (check_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单主表';

-- 盘点明细表
CREATE TABLE IF NOT EXISTS stock_check_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    check_id BIGINT NOT NULL COMMENT '盘点单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    system_quantity DECIMAL(12,4) DEFAULT 0 COMMENT '系统库存',
    actual_quantity DECIMAL(12,4) NOT NULL COMMENT '实际盘点数量',
    difference_quantity DECIMAL(12,4) DEFAULT 0 COMMENT '差异数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (check_id) REFERENCES stock_check_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表';

-- 调拨单主表
CREATE TABLE IF NOT EXISTS stock_transfer_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    transfer_no VARCHAR(50) NOT NULL UNIQUE COMMENT '调拨单号',
    doc_name VARCHAR(200) COMMENT '单据名称',
    from_warehouse_code VARCHAR(50) COMMENT '调出仓库编码',
    from_warehouse_name VARCHAR(200) COMMENT '调出仓库名称',
    to_warehouse_code VARCHAR(50) COMMENT '调入仓库编码',
    to_warehouse_name VARCHAR(200) COMMENT '调入仓库名称',
    total_amount DECIMAL(14,4) DEFAULT 0 COMMENT '总金额',
    status INTEGER DEFAULT 0 COMMENT '状态 0-草稿 1-已审批 2-已调出 3-已完成',
    audit_by BIGINT DEFAULT NULL,
    audit_time DATETIME,
    remark VARCHAR(500) COMMENT '备注',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识',
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_transfer_no (transfer_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨单主表';

-- 调拨明细表
CREATE TABLE IF NOT EXISTS stock_transfer_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    transfer_id BIGINT NOT NULL COMMENT '调拨单ID',
    goods_id BIGINT COMMENT '商品ID',
    goods_code VARCHAR(50) COMMENT '商品编码',
    goods_name VARCHAR(200) COMMENT '商品名称',
    goods_spec VARCHAR(200) COMMENT '商品规格',
    sku_code VARCHAR(50) COMMENT 'SKU编码',
    sku_name VARCHAR(100) COMMENT 'SKU名称',
    unit_code VARCHAR(20) COMMENT '单位',
    transfer_quantity DECIMAL(12,4) NOT NULL COMMENT '调拨数量',
    unit_price DECIMAL(12,4) NOT NULL COMMENT '单价',
    amount DECIMAL(14,4) NOT NULL COMMENT '金额',
    batch_no VARCHAR(50) COMMENT '批次号',
    expire_date DATE COMMENT '有效期',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (transfer_id) REFERENCES stock_transfer_main(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨明细表';

-- ============================================================
-- 工作流模块表结构
-- ============================================================

-- 流程定义表
CREATE TABLE IF NOT EXISTS wf_process_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_key VARCHAR(100) NOT NULL COMMENT '流程唯一标识',
    process_name VARCHAR(200) NOT NULL COMMENT '流程名称',
    version INT DEFAULT 1 COMMENT '版本号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_data_uuid (data_uuid),
    UNIQUE KEY uk_process_key (process_key),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- 流程节点表
CREATE TABLE IF NOT EXISTS wf_process_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_def_id BIGINT NOT NULL COMMENT '流程定义ID',
    node_key VARCHAR(100) NOT NULL COMMENT '节点标识',
    node_name VARCHAR(200) NOT NULL COMMENT '节点名称',
    node_type TINYINT DEFAULT 1 COMMENT '节点类型 1-审批 2-条件 3-抄送 4-结束',
    approve_type TINYINT DEFAULT 1 COMMENT '审批类型 1-单人 2-会签 3-或签',
    sort INT DEFAULT 0 COMMENT '排序',
    config TEXT COMMENT '节点自定义配置（JSON格式）',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_data_uuid (data_uuid),
    INDEX idx_process_def_id (process_def_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点表';

-- 节点流转关系表
CREATE TABLE IF NOT EXISTS wf_process_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_def_id BIGINT NOT NULL COMMENT '流程定义ID',
    from_node_id BIGINT NOT NULL COMMENT '来源节点ID',
    to_node_id BIGINT NOT NULL COMMENT '目标节点ID',
    condition_expr VARCHAR(500) DEFAULT NULL COMMENT 'EL条件表达式',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_data_uuid (data_uuid),
    INDEX idx_process_def_id (process_def_id),
    INDEX idx_from_node (from_node_id),
    INDEX idx_to_node (to_node_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点流转关系表';

-- 流程条件配置表
CREATE TABLE IF NOT EXISTS wf_process_condition_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_def_id BIGINT NOT NULL COMMENT '关联流程定义ID',
    condition_name VARCHAR(100) NOT NULL COMMENT '条件名称',
    condition_key VARCHAR(100) NOT NULL COMMENT '变量key',
    condition_type VARCHAR(20) DEFAULT 'string' COMMENT '类型 number/string/boolean',
    compare_type VARCHAR(10) DEFAULT '=' COMMENT '运算符 > < >= <= =',
    default_value VARCHAR(200) DEFAULT NULL COMMENT '默认值',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_data_uuid (data_uuid),
    INDEX idx_process_def_id (process_def_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程条件配置表';

-- 流程实例表
CREATE TABLE IF NOT EXISTS wf_process_instance (
    id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '实例ID',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_def_id BIGINT NOT NULL COMMENT '流程定义ID',
    process_key VARCHAR(100) NOT NULL COMMENT '流程标识',
    title VARCHAR(200) DEFAULT NULL COMMENT '流程标题',
    start_user_id VARCHAR(64) DEFAULT NULL COMMENT '发起人ID',
    start_user_name VARCHAR(100) DEFAULT NULL COMMENT '发起人姓名',
    current_node_id BIGINT DEFAULT NULL COMMENT '当前节点ID',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待审批 1-审批中 2-已通过 3-已驳回 4-已撤销',
    ext_json TEXT COMMENT '流程变量（JSON）',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_process_def_id (process_def_id),
    INDEX idx_start_user (start_user_id),
    INDEX idx_status (status),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

-- 流程任务表
CREATE TABLE IF NOT EXISTS wf_task (
    id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '任务ID',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    node_id BIGINT DEFAULT NULL COMMENT '节点ID',
    task_name VARCHAR(200) DEFAULT NULL COMMENT '任务名称',
    handler_user_id VARCHAR(64) DEFAULT NULL COMMENT '处理人ID',
    handler_user_name VARCHAR(100) DEFAULT NULL COMMENT '处理人姓名',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待处理 1-已处理 2-已跳过',
    handle_note VARCHAR(500) DEFAULT NULL COMMENT '处理意见',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_handler (handler_user_id),
    INDEX idx_status (status),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程任务表';

-- 流程业务关联表
CREATE TABLE IF NOT EXISTS wf_process_instance_biz (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    biz_type VARCHAR(100) DEFAULT NULL COMMENT '业务类型',
    biz_id VARCHAR(100) DEFAULT NULL COMMENT '业务ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_data_uuid (data_uuid),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_biz_type_biz_id (biz_type, biz_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程业务关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS wf_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    operator_id VARCHAR(64) DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    operate_type TINYINT DEFAULT NULL COMMENT '操作类型 1-发起 2-同意 3-驳回 4-撤销',
    operate_content VARCHAR(500) DEFAULT NULL COMMENT '操作内容',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_data_uuid (data_uuid),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 抄送记录表
CREATE TABLE IF NOT EXISTS wf_cc_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识',
    tenant_id BIGINT DEFAULT NULL COMMENT '租户ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    cc_user_id VARCHAR(64) DEFAULT NULL COMMENT '抄送用户ID',
    cc_user_name VARCHAR(100) DEFAULT NULL COMMENT '抄送用户姓名',
    status TINYINT DEFAULT 1 COMMENT '状态 1-未读 2-已读',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_data_uuid (data_uuid),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_cc_user_id (cc_user_id),
    INDEX idx_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抄送记录表';
