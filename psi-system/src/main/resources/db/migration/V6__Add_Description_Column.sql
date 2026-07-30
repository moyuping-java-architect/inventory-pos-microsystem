USE erp_system_db;

-- 为部门表添加描述字段
ALTER TABLE sys_dept 
ADD COLUMN description VARCHAR(500) DEFAULT NULL COMMENT '描述' AFTER shop_id;