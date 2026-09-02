-- 为缺少 data_uuid 的表添加该字段

ALTER TABLE member_balance_log ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE member_info ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE member_level ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE member_point_log ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE shop_info ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_dept ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_dict_data ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_dict_type ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_login_log ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_menu ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_operation_log ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_role ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_role_menu ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_tenant ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_user ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE sys_user_role ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE up_sync ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE warehouse_info ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE wf_process_instance ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
ALTER TABLE wf_task ADD COLUMN data_uuid VARCHAR(64) DEFAULT NULL COMMENT '数据唯一标识（雪花算法生成）';
