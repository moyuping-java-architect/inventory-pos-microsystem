-- 更新所有 wf_task 的 tenant_id 为 1（与 admin 用户一致）
UPDATE wf_task SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;

-- 更新所有 wf_process_instance 的 tenant_id
UPDATE wf_process_instance SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;

-- 更新所有 wf_process_instance_biz 的 tenant_id
UPDATE wf_process_instance_biz SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;

-- 更新所有 wf_operation_log 的 tenant_id
UPDATE wf_operation_log SET tenant_id = 1 WHERE tenant_id IS NULL OR tenant_id = 0;

-- 验证
SELECT 'wf_task' as tbl, COUNT(*) as null_cnt FROM wf_task WHERE tenant_id IS NULL
UNION ALL
SELECT 'wf_process_instance', COUNT(*) FROM wf_process_instance WHERE tenant_id IS NULL
UNION ALL
SELECT 'wf_process_instance_biz', COUNT(*) FROM wf_process_instance_biz WHERE tenant_id IS NULL
UNION ALL
SELECT 'wf_operation_log', COUNT(*) FROM wf_operation_log WHERE tenant_id IS NULL;
