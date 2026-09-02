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

-- 初始化用户数据 (密码为 MD5 加密的 123456)
INSERT INTO sys_user (id, username, password, nickname, email, phone, dept_id, tenant_id, status, create_time, update_time)
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 'admin@erp.com', '13800138009', 1, 1, 1, NOW(), NOW());

-- 初始化用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始化角色菜单关联
INSERT INTO sys_role_menu (role_id, menu_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11);