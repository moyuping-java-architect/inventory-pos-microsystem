import sys
import pymysql
conn = pymysql.connect(host='127.0.0.1', port=3307, user='root',
                      password='Psi@Db#2026#Root', database='psi_modular', charset='utf8mb4')
cur = conn.cursor()

lines = []
lines.append('=== TOUCHPOINT_TYPE ===')
cur.execute("SELECT config_key, config_value FROM customer_journey_config "
            "WHERE config_group='TOUCHPOINT_TYPE' AND del_flag=0 ORDER BY sort_order, config_key")
for r in cur.fetchall():
    lines.append(f'  {r[0]:24s} = {r[1]}')

lines.append('\n=== INTENT_TYPE ===')
cur.execute("SELECT config_key, config_value FROM customer_journey_config "
            "WHERE config_group='INTENT_TYPE' AND del_flag=0 ORDER BY sort_order, config_key")
for r in cur.fetchall():
    lines.append(f'  {r[0]:24s} = {r[1]}')

lines.append('\n=== SALES_FUNNEL stages ===')
cur.execute("SELECT stage_code, stage_name, match_touchpoint, match_intent "
            "FROM customer_journey_stage "
            "WHERE journey_code='SALES_FUNNEL' AND del_flag=0 ORDER BY sort_order")
for r in cur.fetchall():
    lines.append(f'  {r[0]:24s} {r[1]:10s}  tp={r[2]!r:14s} intent={r[3]!r}')

lines.append('\n=== LIFECYCLE stages ===')
cur.execute("SELECT stage_code, stage_name, match_touchpoint, match_intent "
            "FROM customer_journey_stage "
            "WHERE journey_code='LIFECYCLE' AND del_flag=0 ORDER BY sort_order")
for r in cur.fetchall():
    lines.append(f'  {r[0]:24s} {r[1]:10s}  tp={r[2]!r:14s} intent={r[3]!r}')

lines.append('\n=== customer_touchpoint sample ===')
cur.execute("SELECT touchpoint_type, channel, intent FROM customer_touchpoint WHERE del_flag=0 LIMIT 20")
for r in cur.fetchall():
    lines.append(f'  tp={r[0]:24s} ch={r[1]:10s} intent={r[2]}')

with open(r'E:\spring boot\psi-modular\db\__qa_tp_intent_out.txt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))

