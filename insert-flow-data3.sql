-- ============================================================
-- 采购审批流程（带条件分支）
-- 逻辑: 发起人 -> 条件判断(金额>10000?) -> 是: 财务审批 -> 结束 | 否: 直接结束
-- ============================================================

-- 1. 清理旧数据
DELETE FROM wf_process_relation WHERE process_def_id = 1;
DELETE FROM wf_process_node WHERE process_def_id = 1;
DELETE FROM wf_process_condition_config WHERE process_def_id = 1;
DELETE FROM wf_process_instance WHERE process_def_id = 1;

-- 2. 流程定义
REPLACE INTO wf_process_definition (id, process_key, process_name, version, remark, status, del_flag, tenant_id, create_time, update_time)
VALUES (1, 'PURCHASE_APPROVAL', '采购审批流程', 1, '根据采购金额自动判断审批流程，>10000元需财务审批，≤10000元直接通过', 1, 0, 1, NOW(), NOW());

-- 3. 节点定义
-- node_type: 1=审批, 2=条件, 3=抄送, 4=结束
REPLACE INTO wf_process_node (id, process_def_id, node_key, node_name, node_type, approve_type, sort, config, status, del_flag, tenant_id, create_time, update_time) VALUES
(1, 1, 'DEPARTMENT_APPROVAL', '部门审批', 1, 1, 1, NULL, 1, 0, 1, NOW(), NOW()),
(2, 1, 'AMOUNT_CONDITION', '金额判断', 2, 1, 2, NULL, 1, 0, 1, NOW(), NOW()),
(3, 1, 'FINANCE_APPROVAL', '财务审批', 1, 1, 3, NULL, 1, 0, 1, NOW(), NOW()),
(4, 1, 'END', '流程结束', 4, 1, 99, NULL, 1, 0, 1, NOW(), NOW());

-- 4. 节点流转关系 (条件分支)
-- 部门审批 -> 条件节点 (必经)
REPLACE INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time)
VALUES (1, 1, 1, 2, NULL, 1, 0, 1, NOW(), NOW());

-- 条件节点 -> 财务审批 (条件: amount > 10000)
REPLACE INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time)
VALUES (2, 1, 2, 3, 'amount > 10000', 1, 0, 1, NOW(), NOW());

-- 条件节点 -> 结束节点 (条件: amount <= 10000)
REPLACE INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time)
VALUES (3, 1, 2, 4, 'amount <= 10000', 1, 0, 1, NOW(), NOW());

-- 财务审批 -> 结束节点
REPLACE INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time)
VALUES (4, 1, 3, 4, NULL, 1, 0, 1, NOW(), NOW());

-- 5. 流程条件变量配置 (前端展示用)
REPLACE INTO wf_process_condition_config (id, process_def_id, condition_name, condition_key, condition_type, compare_type, default_value, sort, status, del_flag, tenant_id, create_time, update_time)
VALUES (1, 1, '采购金额', 'amount', 'number', '>', '10000', 1, 1, 0, 1, NOW(), NOW());

-- ============================================================
-- 销售审批流程（简化，单节点）
-- ============================================================
DELETE FROM wf_process_relation WHERE process_def_id = 2;
DELETE FROM wf_process_node WHERE process_def_id = 2;
DELETE FROM wf_process_instance WHERE process_def_id = 2;

REPLACE INTO wf_process_definition (id, process_key, process_name, version, remark, status, del_flag, tenant_id, create_time, update_time)
VALUES (2, 'SALE_APPROVAL', '销售审批流程', 1, '通用销售订单审批', 1, 0, 1, NOW(), NOW());

REPLACE INTO wf_process_node (id, process_def_id, node_key, node_name, node_type, approve_type, sort, config, status, del_flag, tenant_id, create_time, update_time) VALUES
(5, 2, 'MANAGER_APPROVAL', '部门经理审批', 1, 1, 1, NULL, 1, 0, 1, NOW(), NOW()),
(6, 2, 'END', '流程结束', 4, 1, 99, NULL, 1, 0, 1, NOW(), NOW());

REPLACE INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time)
VALUES (5, 2, 5, 6, NULL, 1, 0, 1, NOW(), NOW());

-- ============================================================
-- 验证查询
-- ============================================================
SELECT '流程定义' AS type, id, process_key, process_name FROM wf_process_definition WHERE status = 1;
SELECT '节点' AS type, id, process_def_id, node_name, node_type, sort FROM wf_process_node WHERE process_def_id IN (1, 2) ORDER BY process_def_id, sort;
SELECT '关系' AS type, id, process_def_id, from_node_id, to_node_id, condition_expr FROM wf_process_relation WHERE process_def_id IN (1, 2);
SELECT '条件配置' AS type, id, process_def_id, condition_name, condition_key, compare_type, default_value FROM wf_process_condition_config WHERE process_def_id = 1;