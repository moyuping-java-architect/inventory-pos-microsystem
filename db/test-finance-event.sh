#!/bin/bash
MYSQL="docker exec -i psi-mysql mysql -uroot -pPsi@Db#2026#Root psi_modular"
API="http://localhost:8080"
q() { $MYSQL 2>/dev/null -N -e "$1"; }

echo "########## F0: 建测试应收单(客户 Alice id=1) ##########"
$MYSQL 2>/dev/null <<SQL
INSERT INTO finance_receivable (store_code, store_name, customer_id, customer_code, customer_name, total_amount, paid_amount, remain_amount, source_no, source_type, bill_date, due_date, tenant_id, del_flag, status)
VALUES ('TEST','测试店', 1, 'DEMO-CUST-001', 'Alice Mwanza', 1000.0000, 0.0000, 1000.0000, CONCAT('RXTEST', UNIX_TIMESTAMP()), 'TEST', '2026-08-08', '2026-09-08', 1, 0, 1);
SQL
RID=$(q "SELECT id FROM finance_receivable WHERE source_no LIKE 'RXTEST%' ORDER BY id DESC LIMIT 1" | tail -1)
echo "RECEIVABLE_ID=$RID"

echo "########## F1: 回款 500 (PAYTEST001) ##########"
curl -s -X POST "$API/psi/finance/receivable/$RID/pay" -H "Content-Type: application/json" -d '{"payAmount":500,"payMethod":"CASH","payNo":"PAYTEST001","payDate":"2026-08-08"}'; echo

echo ""
echo "================ 验证报告(财务) ================"
echo "--- PAYMENT_RECEIVED 触点(customer_id=1) ---"
q "SELECT id, customer_id, touchpoint_type, intent, LEFT(summary,60) FROM customer_touchpoint WHERE customer_id=1 AND touchpoint_type='PAYMENT_RECEIVED' ORDER BY id"
echo "--- 回款事件触发日志数 ---"
q "SELECT COUNT(*) FROM journey_rule_fire_log WHERE subject_id=1 AND event_code='FINANCE.PAYMENT_RECEIVED'"
echo "--- Alice 所属旅程状态 ---"
q "SELECT CONCAT(journey_code,' stage=',current_stage_code,' last=',last_event_code) FROM customer_journey_state WHERE subject_id=1"
echo "RECEIVABLE_ID=$RID"
