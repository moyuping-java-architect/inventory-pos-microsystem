# -*- coding: utf-8 -*-
"""
触点编码统一 + 阶段直连 迁移（幂等）。

解决的问题：三层编码各说各话，导致老板配了旅程也永远推不动。
  第1层 business_event_dict.event_code        业务事件源（26个）
  第2层 customer_touchpoint.touchpoint_type   实际落库触点
  第3层 customer_journey_stage.match_touchpoint 阶段匹配

历史上这三层是三套编码：SALES_FUNNEL 的阶段要 INQUIRY/STORE_VISIT/DOC_SENT/
CONTRACT_SIGNED，可实际库里躺着的是 IM/VISIT/DOC/CONTRACT —— 永远匹配不上。

本脚本做三件事：
  1. customer_touchpoint 加 source_rule_id —— 触点能倒查是哪条规则生成的，
     引擎据此直连阶段，彻底绕开 match_touchpoint 的字符串比对
  2. 修 touchpoint_generate_rule 里双重编码的乱码规则名与摘要模板
  3. 历史短码归一 + 给 SALES_FUNNEL 断链的阶段补上生成规则

触点类型字典不在这里维护：journey_dict(dict_type='TOUCHPOINT_TYPE') 才是真相源，
英文名由 migrate_dict_i18n.py 负责。
"""
import pymysql

HOST, PORT, USER, PW, DB = '127.0.0.1', 3307, 'root', 'Psi@Db#2026#Root', 'psi_modular'

# 历史短码 -> 标准码
LEGACY_REMAP = {
    'IM':       'INQUIRY',
    'VISIT':    'STORE_VISIT',
    'DOC':      'DOC_SENT',
    'CONTRACT': 'CONTRACT_SIGNED',
}

# SALES_FUNNEL 断链补规则：(规则名, 事件码, 触点类型, 阶段码, 意图, 摘要模板, 排序)
FUNNEL_RULES = [
    ('客户建档视为意向', 'CUSTOMER.CREATED',        'INQUIRY',     'STAGE_INTENTION', 'FIRST_CONTACT',
     '客户建档，进入意向阶段', 210),
    ('订单送审视为谈判', 'SALE.ORDER_SUBMITTED',    'NEGOTIATION', 'STAGE_PRICE',     'QUOTE_GIVEN',
     '订单送审，进入价格谈判', 220),
    ('发货视为方案交付', 'SALE.ORDER_SHIPPED',      'DOC_SENT',    'STAGE_PROPOSAL',  'SCHEMA_DISCUSS',
     '已发货，方案交付客户', 230),
]


def column_exists(cur, table, column):
    cur.execute("""
        SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA=%s AND TABLE_NAME=%s AND COLUMN_NAME=%s
    """, (DB, table, column))
    return cur.fetchone()[0] > 0


def table_exists(cur, table):
    cur.execute("""
        SELECT COUNT(*) FROM information_schema.TABLES
        WHERE TABLE_SCHEMA=%s AND TABLE_NAME=%s
    """, (DB, table))
    return cur.fetchone()[0] > 0


def main():
    conn = pymysql.connect(host=HOST, port=PORT, user=USER, password=PW,
                           database=DB, charset='utf8mb4', autocommit=False)
    cur = conn.cursor()

    # ---- 1. 触点溯源列 ----
    if not column_exists(cur, 'customer_touchpoint', 'source_rule_id'):
        cur.execute("""
            ALTER TABLE customer_touchpoint
            ADD COLUMN source_rule_id BIGINT NULL COMMENT '生成该触点的规则ID，用于直连阶段'
            AFTER intent
        """)
        print('[OK] customer_touchpoint 增加 source_rule_id')
    else:
        print('[SKIP] source_rule_id 已存在')

    # ---- 2. 修复双重编码的规则名 ----
    cur.execute("""
        SELECT id, rule_name FROM touchpoint_generate_rule
        WHERE rule_name IS NOT NULL AND HEX(rule_name) LIKE '%C3A4%'
           OR HEX(rule_name) LIKE '%C3A5%' OR HEX(rule_name) LIKE '%C3A6%'
           OR HEX(rule_name) LIKE '%C3A7%' OR HEX(rule_name) LIKE '%C3A8%'
           OR HEX(rule_name) LIKE '%C3A9%'
    """)
    broken = cur.fetchall()
    if broken:
        cur.execute("""
            UPDATE touchpoint_generate_rule
            SET rule_name = CONVERT(BINARY(CONVERT(rule_name USING latin1)) USING utf8mb4)
            WHERE id IN (%s)
        """ % ','.join(str(r[0]) for r in broken))
        print(f'[OK] 修复 {len(broken)} 条乱码规则名')
    else:
        print('[SKIP] 无乱码规则名')

    # 摘要模板同样可能被双重编码
    cur.execute("""
        UPDATE touchpoint_generate_rule
        SET summary_template = CONVERT(BINARY(CONVERT(summary_template USING latin1)) USING utf8mb4)
        WHERE summary_template IS NOT NULL
          AND (HEX(summary_template) LIKE '%C3A4%' OR HEX(summary_template) LIKE '%C3A5%'
            OR HEX(summary_template) LIKE '%C3A6%' OR HEX(summary_template) LIKE '%C3A8%'
            OR HEX(summary_template) LIKE '%C3A9%')
    """)
    if cur.rowcount:
        print(f'[OK] 修复 {cur.rowcount} 条乱码摘要模板')

    # ---- 3. 历史短码归一 ----
    total = 0
    for legacy, std in LEGACY_REMAP.items():
        cur.execute("UPDATE customer_touchpoint SET touchpoint_type=%s WHERE touchpoint_type=%s",
                    (std, legacy))
        if cur.rowcount:
            print(f'     {legacy} -> {std}: {cur.rowcount} 条')
            total += cur.rowcount
    print(f'[OK] 历史触点归一 {total} 条')

    # 阶段侧同样归一（防止有人手工填了短码）
    for legacy, std in LEGACY_REMAP.items():
        cur.execute("UPDATE customer_journey_stage SET match_touchpoint=%s WHERE match_touchpoint=%s",
                    (std, legacy))
    for legacy, std in LEGACY_REMAP.items():
        cur.execute("UPDATE touchpoint_generate_rule SET touchpoint_type=%s WHERE touchpoint_type=%s",
                    (std, legacy))

    # ---- 4. 补 SALES_FUNNEL 断链规则 ----
    added = 0
    for name, event, tp_type, stage, intent, summary, order in FUNNEL_RULES:
        cur.execute("""
            SELECT COUNT(*) FROM touchpoint_generate_rule
            WHERE event_code=%s AND touchpoint_type=%s AND del_flag=0
        """, (event, tp_type))
        if cur.fetchone()[0] > 0:
            continue
        cur.execute("""
            INSERT INTO touchpoint_generate_rule
                (tenant_id, rule_name, event_code, touchpoint_type, stage_code, channel,
                 intent, summary_template, condition_json, once_only, sort_order,
                 enabled, del_flag, create_time, update_time)
            VALUES (NULL, %s, %s, %s, %s, 'SYSTEM', %s, %s, NULL, 0, %s, 1, 0, NOW(), NOW())
        """, (name, event, tp_type, stage, intent, summary, order))
        added += 1
        print(f'     + {name}: {event} -> {tp_type} -> {stage}')
    print(f'[OK] 补充 {added} 条销售漏斗规则')

    # ---- 5. 回填已有规则的 stage_code（按 touchpoint_type + intent 反查阶段） ----
    cur.execute("""
        UPDATE touchpoint_generate_rule r
        JOIN customer_journey_stage s
          ON s.match_touchpoint = r.touchpoint_type
         AND s.del_flag = 0
         AND (s.match_intent IS NULL OR s.match_intent = '' OR s.match_intent = r.intent)
        SET r.stage_code = s.stage_code
        WHERE r.stage_code IS NULL AND r.del_flag = 0
    """)
    print(f'[OK] 回填 {cur.rowcount} 条规则的 stage_code')

    conn.commit()

    # ---- 汇总校验 ----
    print('\n===== 校验：阶段 match_touchpoint 是否都有规则能产生 =====')
    cur.execute("""
        SELECT s.journey_code, s.stage_code, s.match_touchpoint,
               (SELECT COUNT(*) FROM touchpoint_generate_rule r
                 WHERE r.touchpoint_type = s.match_touchpoint AND r.del_flag = 0) AS rule_cnt
        FROM customer_journey_stage s
        WHERE s.del_flag = 0
        ORDER BY s.journey_code, s.sort_order
    """)
    for jc, sc, mt, cnt in cur.fetchall():
        flag = 'OK ' if cnt > 0 else '断链'
        print(f'  [{flag}] {jc:<14} {sc:<18} {mt:<18} rules={cnt}')

    cur.close()
    conn.close()
    print('\n迁移完成。')


if __name__ == '__main__':
    main()
