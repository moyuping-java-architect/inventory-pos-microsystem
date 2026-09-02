import pymysql
conn = pymysql.connect(host='127.0.0.1', port=3307, user='root',
                      password='Psi@Db#2026#Root', database='psi_modular', charset='utf8mb4')
cur = conn.cursor()
out = []
cur.execute("SELECT DISTINCT touchpoint_type, intent, channel FROM touchpoint_generate_rule WHERE del_flag=0 ORDER BY touchpoint_type")
rows = cur.fetchall()
out.append(f'touchpoint_generate_rule 实际去重后的 touchpoint_type 数: {len(rows)}\n')
for r in rows:
    out.append(f'  tp={r[0]:22s} intent={str(r[1]):14s} ch={r[2]}')
cur.execute("SELECT DISTINCT touchpoint_type FROM customer_touchpoint WHERE del_flag=0 ORDER BY touchpoint_type")
tp_rows = [r[0] for r in cur.fetchall()]
out.append(f'\n\ncustomer_touchpoint 实际出现过的 touchpoint_type ({len(tp_rows)}):')
out.append('  ' + ', '.join(tp_rows))
conn.close()
with open(r'E:\spring boot\psi-modular\db\__check_tp_out.txt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(out))
