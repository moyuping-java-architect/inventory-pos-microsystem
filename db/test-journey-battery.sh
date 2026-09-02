#!/bin/bash
MYSQL="docker exec -i psi-mysql mysql -uroot -pPsi@Db#2026#Root psi_modular"
API="http://localhost:8080"
PHONE="13800000998"
TS=$(date +%s)

q() { $MYSQL 2>/dev/null -N -e "$1"; }

echo "########## S0: 注册测试会员 (phone=$PHONE) ##########"
curl -s -X POST "$API/member/info" -H "Content-Type: application/json" -d "{\"memberName\":\"测试旅程\",\"phone\":\"$PHONE\"}"; echo
MID=$(q "SELECT id FROM member_info WHERE phone='$PHONE' ORDER BY id DESC LIMIT 1" | tail -1)
echo "MEMBER_ID=$MID"

chk() { echo "--- $1 ---"; }

echo "########## S1: 充值 60000 (R1) ##########"
curl -s -X POST "$API/member/info/recharge?memberId=$MID&amount=60000&sourceNo=JT${TS}_R1"; echo

echo "########## S2: 首消 1200/积分1200 (C1) ##########"
curl -s -X POST "$API/member/info/consume?memberId=$MID&amount=1200&points=1200&sourceNo=JT${TS}_C1"; echo

echo "########## S3: 再消 1200/积分0 (C2) ##########"
curl -s -X POST "$API/member/info/consume?memberId=$MID&amount=1200&points=0&sourceNo=JT${TS}_C2"; echo

echo "########## S4: 消 5000 (C3) -> 期望跨银卡 ##########"
curl -s -X POST "$API/member/info/consume?memberId=$MID&amount=5000&points=0&sourceNo=JT${TS}_C3"; echo

echo "########## S5: 消 20000 (C4) -> 期望跨金卡 ##########"
curl -s -X POST "$API/member/info/consume?memberId=$MID&amount=20000&points=0&sourceNo=JT${TS}_C4"; echo

echo "########## S6: 消 30000 (C5) -> 期望跨黑卡 ##########"
curl -s -X POST "$API/member/info/consume?memberId=$MID&amount=30000&points=0&sourceNo=JT${TS}_C5"; echo

echo "########## S7: 再消 100 (C6) -> 期望无新升级触点(幂等) ##########"
curl -s -X POST "$API/member/info/consume?memberId=$MID&amount=100&points=0&sourceNo=JT${TS}_C6"; echo

echo "########## S8: 负向-余额不足 99999 (C7) -> 期望500 ##########"
curl -s -X POST "$API/member/info/consume?memberId=$MID&amount=99999&points=0&sourceNo=JT${TS}_C7"; echo

echo "########## S9: 重放 C1 (同sourceNo) -> 验证事件重放行为 ##########"
curl -s -X POST "$API/member/info/consume?memberId=$MID&amount=1200&points=1200&sourceNo=JT${TS}_C1"; echo

echo ""
echo "================ 验证报告 ================"
echo "--- 会员账户 ---"
q "SELECT CONCAT('balance=',balance,' points=',points,' total_consume=',total_consume,' total_orders=',total_orders) FROM member_info WHERE id=$MID"
echo "--- 触点按类型计数 ---"
q "SELECT CONCAT(touchpoint_type,'/',IFNULL(intent,''),': ',COUNT(*)) FROM customer_touchpoint WHERE member_id=$MID GROUP BY touchpoint_type,intent ORDER BY touchpoint_type"
echo "--- 旅程状态 ---"
q "SELECT CONCAT(journey_code,' stage=',current_stage_code,' order=',current_stage_order,' advance=',advance_count,' last=',last_event_code) FROM customer_journey_state WHERE subject_id=$MID"
echo "--- 规则触发日志数 ---"
q "SELECT COUNT(*) FROM journey_rule_fire_log WHERE subject_id=$MID"
echo "--- 余额不足C7后余额(应仍=2600-100=2500...实际S8失败S7后=2500) ---"
echo "MEMBER_ID=$MID  TS=$TS"
