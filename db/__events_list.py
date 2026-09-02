import pymysql
conn = pymysql.connect(host='127.0.0.1', port=3307, user='root',
                      password='Psi@Db#2026#Root', database='psi_modular', charset='utf8mb4')
cur = conn.cursor()
print('=== business_event_dict 全部事件 ===')
cur.execute("SELECT event_code, event_name, module_name, subject_type, implemented, enabled "
            "FROM business_event_dict ORDER BY module_name, sort_order, event_code")
rows = cur.fetchall()
print('count =', len(rows))
for r in rows:
    print(f'{r[0]:28s} | {r[1]:14s} | mod={r[2]:8s} | subj={r[3]:8s} | impl={r[4]} | en={r[5]}')
print()
print('=== touchpoint_generate_rule 现有 stage_code 列? ===')
cur.execute("SELECT COLUMN_NAME FROM information_schema.COLUMNS "
            "WHERE TABLE_SCHEMA='psi_modular' AND TABLE_NAME='touchpoint_generate_rule' AND COLUMN_NAME='stage_code'")
print('stage_code exists:', bool(cur.fetchone()))
cur.execute("SELECT COLUMN_NAME FROM information_schema.COLUMNS "
            "WHERE TABLE_SCHEMA='psi_modular' AND TABLE_NAME='business_event_dict' AND COLUMN_NAME='event_name_en'")
print('event_name_en exists:', bool(cur.fetchone()))
conn.close()
