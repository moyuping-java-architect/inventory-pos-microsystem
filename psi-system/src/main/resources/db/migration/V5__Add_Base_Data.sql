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