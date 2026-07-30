import subprocess

def run_mysql(sql):
    cmd = ['mysql', '-u', 'root', '-p123456', 'psi_flow', '-e', sql]
    result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
    if result.returncode != 0:
        print(f"ERROR: {result.stderr.strip()}")
        print(f"SQL: {sql[:100]}...")
        return False
    return True

# ============================================================
# 清理旧数据
# ============================================================
print("清理旧数据...")
run_mysql("DELETE FROM wf_process_relation WHERE process_def_id IN (1,2)")
run_mysql("DELETE FROM wf_process_node WHERE process_def_id IN (1,2)")
run_mysql("DELETE FROM wf_process_condition_config WHERE process_def_id = 1")
run_mysql("DELETE FROM wf_process_instance WHERE process_def_id IN (1,2)")
run_mysql("DELETE FROM wf_task WHERE 1=1")
run_mysql("DELETE FROM wf_operation_log WHERE 1=1")

# ============================================================
# 采购审批流程 - 带条件分支
# 逻辑: 部门审批 -> 条件判断(金额>10000?) -> 是: 财务审批 -> 结束 | 否: 结束
# ============================================================
print("插入采购审批流程...")

# 流程定义
run_mysql("INSERT INTO wf_process_definition (id, process_key, process_name, version, remark, status, del_flag, tenant_id, create_time, update_time) VALUES (1, 'PURCHASE_APPROVAL', '采购审批流程', 1, '根据采购金额自动判断审批流程，大于10000元需财务审批', 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE process_name='采购审批流程', remark='根据采购金额自动判断审批流程，大于10000元需财务审批'")

# 节点: 1=部门审批, 2=条件节点(金额判断), 3=财务审批, 4=结束节点
run_mysql("INSERT INTO wf_process_node (id, process_def_id, node_key, node_name, node_type, approve_type, sort, status, del_flag, tenant_id, create_time, update_time) VALUES (1, 1, 'DEPARTMENT_APPROVAL', '部门审批', 1, 1, 1, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE node_name='部门审批', sort=1")
run_mysql("INSERT INTO wf_process_node (id, process_def_id, node_key, node_name, node_type, approve_type, sort, status, del_flag, tenant_id, create_time, update_time) VALUES (2, 1, 'AMOUNT_CONDITION', '金额判断', 2, 1, 2, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE node_name='金额判断', sort=2")
run_mysql("INSERT INTO wf_process_node (id, process_def_id, node_key, node_name, node_type, approve_type, sort, status, del_flag, tenant_id, create_time, update_time) VALUES (3, 1, 'FINANCE_APPROVAL', '财务审批', 1, 1, 3, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE node_name='财务审批', sort=3")
run_mysql("INSERT INTO wf_process_node (id, process_def_id, node_key, node_name, node_type, approve_type, sort, status, del_flag, tenant_id, create_time, update_time) VALUES (4, 1, 'END', '流程结束', 4, 1, 99, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE node_name='流程结束', sort=99")

# 节点流转关系
# 1. 部门审批 -> 条件节点
run_mysql("INSERT INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time) VALUES (1, 1, 1, 2, NULL, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE condition_expr=NULL")
# 2. 条件节点 -> 财务审批 (条件: amount > 10000)
run_mysql("INSERT INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time) VALUES (2, 1, 2, 3, 'amount > 10000', 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE condition_expr='amount > 10000'")
# 3. 条件节点 -> 结束节点 (条件: amount <= 10000)
run_mysql("INSERT INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time) VALUES (3, 1, 2, 4, 'amount <= 10000', 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE condition_expr='amount <= 10000'")
# 4. 财务审批 -> 结束节点
run_mysql("INSERT INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time) VALUES (4, 1, 3, 4, NULL, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE condition_expr=NULL")

# 流程条件变量配置
run_mysql("INSERT INTO wf_process_condition_config (id, process_def_id, condition_name, condition_key, condition_type, compare_type, default_value, sort, status, del_flag, tenant_id, create_time, update_time) VALUES (1, 1, '采购金额', 'amount', 'number', '>', '10000', 1, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE condition_name='采购金额'")

# ============================================================
# 销售审批流程（简化，单节点审批）
# ============================================================
print("插入销售审批流程...")

run_mysql("INSERT INTO wf_process_definition (id, process_key, process_name, version, remark, status, del_flag, tenant_id, create_time, update_time) VALUES (2, 'SALE_APPROVAL', '销售审批流程', 1, '通用销售订单审批', 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE process_name='销售审批流程'")

run_mysql("INSERT INTO wf_process_node (id, process_def_id, node_key, node_name, node_type, approve_type, sort, status, del_flag, tenant_id, create_time, update_time) VALUES (5, 2, 'MANAGER_APPROVAL', '部门经理审批', 1, 1, 1, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE node_name='部门经理审批'")
run_mysql("INSERT INTO wf_process_node (id, process_def_id, node_key, node_name, node_type, approve_type, sort, status, del_flag, tenant_id, create_time, update_time) VALUES (6, 2, 'END', '流程结束', 4, 1, 99, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE node_name='流程结束'")

run_mysql("INSERT INTO wf_process_relation (id, process_def_id, from_node_id, to_node_id, condition_expr, status, del_flag, tenant_id, create_time, update_time) VALUES (5, 2, 5, 6, NULL, 1, 0, 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE condition_expr=NULL")

# ============================================================
# 验证
# ============================================================
print("\n===== 验证数据 =====")
result = subprocess.run(['mysql', '-u', 'root', '-p123456', 'psi_flow', '-e',
    "SELECT id, process_key, process_name FROM wf_process_definition WHERE status=1;"],
    capture_output=True, text=True, encoding='utf-8')
print("流程定义:")
print(result.stdout)

result = subprocess.run(['mysql', '-u', 'root', '-p123456', 'psi_flow', '-e',
    "SELECT id, process_def_id, node_name, node_type, sort FROM wf_process_node WHERE status=1 AND del_flag=0 ORDER BY process_def_id, sort;"],
    capture_output=True, text=True, encoding='utf-8')
print("节点:")
print(result.stdout)

result = subprocess.run(['mysql', '-u', 'root', '-p123456', 'psi_flow', '-e',
    "SELECT id, process_def_id, from_node_id, to_node_id, condition_expr FROM wf_process_relation WHERE status=1 AND del_flag=0 ORDER BY id;"],
    capture_output=True, text=True, encoding='utf-8')
print("关系:")
print(result.stdout)

result = subprocess.run(['mysql', '-u', 'root', '-p123456', 'psi_flow', '-e',
    "SELECT id, process_def_id, condition_name, condition_key, compare_type, default_value FROM wf_process_condition_config WHERE status=1 AND del_flag=0;"],
    capture_output=True, text=True, encoding='utf-8')
print("条件配置:")
print(result.stdout)

print("\n===== 完成 =====")