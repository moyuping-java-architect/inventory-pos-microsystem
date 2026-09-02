import pymysql
conn = pymysql.connect(host='127.0.0.1', port=3307, user='root',
                      password='Psi@Db#2026#Root', database='psi_modular', charset='utf8mb4')
cur = conn.cursor()
out = []
cur.execute("SELECT event_code, event_name, module_name, subject_type, implemented, enabled FROM business_event_dict ORDER BY sort_order")
rows = cur.fetchall()
out.append(f'DB business_event_dict 实际行数: {len(rows)}\n')
for r in rows:
    flag = 'OK' if r[4] == 1 else 'PENDING'
    out.append(f'  [{flag}] {r[0]:28s} {r[1]}  ({r[2]}/{r[3]})')
cur.execute("SELECT COUNT(*) FROM business_event_dict WHERE implemented=1")
out.append(f'\nplugged-in: {cur.fetchone()[0]}')
conn.close()
with open(r'E:\spring boot\psi-modular\db\__check_events_out.txt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(out))
