USE erp_system_db;

-- 更新部门的 shop_id 字段
UPDATE sys_dept SET shop_id = 1 WHERE id IN (1, 2, 3);
UPDATE sys_dept SET shop_id = 2 WHERE id = 4;