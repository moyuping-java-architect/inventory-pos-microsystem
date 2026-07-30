USE erp_system_db;

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