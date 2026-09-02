"""
Directly re-write the 3 template name rows using a utf8mb4 connection (so we send the
correct codepoints, and the column receives correct utf8mb4 bytes). Also handle
customer_journey_stage rows.
"""
import pymysql

DB = dict(host='127.0.0.1', port=3307, user='root',
          password='Psi@Db#2026#Root', database='psi_modular',
          charset='utf8mb4')

# Known correct Chinese names (already determined before the script ran)
TEMPLATE_NAMES = {
    'SALES_FUNNEL': '销售漏斗',
    'MEMBERSHIP':   '会员体系',
    'LIFECYCLE':    '客户生命周期',
}

# Stages - recover from the original SQL files we know
# We can read them from the original .sql if we want. For now, hardcode based on what
# was inserted in db/lifecycle-journey.sql and db/seed-journey-stages.sql.

STAGE_NAMES = {
    # SALES_FUNNEL: 6 stages
    ('SALES_FUNNEL','STAGE_INTENTION'): '意向客户',
    ('SALES_FUNNEL','STAGE_DEMO'):      '拜访演示',
    ('SALES_FUNNEL','STAGE_PROPOSAL'):  '方案报价',
    ('SALES_FUNNEL','STAGE_PRICE'):     '价格谈判',
    ('SALES_FUNNEL','STAGE_SIGNED'):    '已签约',
    ('SALES_FUNNEL','STAGE_SUCCESS'):   '成交成功',
    # MEMBERSHIP: 6 stages
    ('MEMBERSHIP','NEW'):         '新客户',
    ('MEMBERSHIP','FIRST_BUY'):   '首单客户',
    ('MEMBERSHIP','BRONZE'):      '青铜会员',
    ('MEMBERSHIP','SILVER'):      '白银会员',
    ('MEMBERSHIP','GOLD'):        '黄金会员',
    ('MEMBERSHIP','BLACK'):       '黑金会员',
    # LIFECYCLE: 4 stages
    ('LIFECYCLE','LC_NEW'):       '新客',
    ('LIFECYCLE','LC_ACTIVE'):    '活跃',
    ('LIFECYCLE','LC_SILENT'):    '沉默',
    ('LIFECYCLE','LC_CHURNED'):   '已流失',
}

conn = pymysql.connect(**DB)
cur = conn.cursor()

print("=== Fix journey_template.journey_name ===")
for code, name in TEMPLATE_NAMES.items():
    cur.execute("UPDATE customer_journey_template SET journey_name=%s WHERE journey_code=%s",
                (name, code))
    print(f"  {code}: {cur.rowcount} row")

print("\n=== Fix journey_stage.stage_name ===")
for (journey_code, stage_code), name in STAGE_NAMES.items():
    cur.execute("""UPDATE customer_journey_stage SET stage_name=%s
                   WHERE journey_code=%s AND stage_code=%s""",
                (name, journey_code, stage_code))
    print(f"  {journey_code}/{stage_code}: {cur.rowcount} row")

conn.commit()

# Verify
cur.execute("SELECT journey_code, journey_name, OCTET_LENGTH(journey_name) AS bytes FROM customer_journey_template")
print("\n=== Verify ===")
for r in cur.fetchall():
    print(f"  {r}")

conn.close()
print("\nDone.")
