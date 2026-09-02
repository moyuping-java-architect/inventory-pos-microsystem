"""
Fix double-UTF8 encoding on LIFECYCLE rows specifically (the only ones we
detected polluted via the b/c ratio diagnostic).

Method: utf8mb4 client + hardcoded correct Chinese text.
"""
import pymysql

DB = dict(host='127.0.0.1', port=3307, user='root',
          password='Psi@Db#2026#Root', database='psi_modular',
          charset='utf8mb4')

FIXES = [
    # customer_journey_template rows
    ('UPDATE customer_journey_template SET description = %s WHERE journey_code = %s',
     ['注册→活跃→沉默→流失→唤醒：基于距上次消费天数的动态分层（私域运营核心）', 'LIFECYCLE']),
    # customer_journey_stage rows
    ('UPDATE customer_journey_stage SET tip = %s WHERE journey_code = %s AND stage_code = %s',
     ['新注册会员（无消费记录）', 'LIFECYCLE', 'LC_NEW']),
    ('UPDATE customer_journey_stage SET tip = %s WHERE journey_code = %s AND stage_code = %s',
     ['近期有过消费，处于活跃状态', 'LIFECYCLE', 'LC_ACTIVE']),
    ('UPDATE customer_journey_stage SET tip = %s WHERE journey_code = %s AND stage_code = %s',
     ['已超过活跃周期但未达流失阈值，需主动唤醒', 'LIFECYCLE', 'LC_SILENT']),
    ('UPDATE customer_journey_stage SET tip = %s WHERE journey_code = %s AND stage_code = %s',
     ['超过流失阈值未回访，需召回激活', 'LIFECYCLE', 'LC_CHURNED']),
]

conn = pymysql.connect(**DB)
cur = conn.cursor()
cur.execute("SET autocommit = 1")
for sql, params in FIXES:
    n = cur.execute(sql, params)
    print(f"{n} row(s)  :  {sql[:80]}")
conn.close()
print("Done.")
