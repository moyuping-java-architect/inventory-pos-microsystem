# -*- coding: utf-8 -*-
"""
旅程字典国际化 + 去重（幂等）。

背景：上一版脚本另起炉灶建了 touchpoint_type_dict，属于误判——
journey_dict 早就是全系统字典的真相源（TOUCHPOINT_TYPE / INTENT / CHANNEL 三类都在里面）。
两套字典并存必然会漂移，所以这里把新表删掉，回到 journey_dict 一处维护。

本脚本做三件事：
  1. DROP 掉重复的 touchpoint_type_dict
  2. journey_dict 增加 dict_name_en，给全部触点/意图/渠道补英文名
  3. 修掉 7 条双重编码的乱码字典名（沉默预警/流失/二次关怀/唤醒/沉默/流失/培育）
"""
import pymysql

HOST, PORT, USER, PW, DB = '127.0.0.1', 3307, 'root', 'Psi@Db#2026#Root', 'psi_modular'

# dict_type -> {dict_code: (中文名, 英文名)}
DICTS = {
    'TOUCHPOINT_TYPE': {
        'PROFILE_CREATED':  ('客户建档',        'Profile created'),
        'INQUIRY':          ('问价咨询',        'Inquiry'),
        'WHATSAPP_MSG':     ('WhatsApp 消息',   'WhatsApp message'),
        'PHONE_CALL':       ('电话沟通',        'Phone call'),
        'STORE_VISIT':      ('到店/上门',       'Store or site visit'),
        'DOC_SENT':         ('发送方案报价',    'Proposal sent'),
        'NEGOTIATION':      ('议价洽谈',        'Price negotiation'),
        'CONTRACT_SIGNED':  ('合同签订',        'Contract signed'),
        'SILENT_WARNING':   ('沉默预警',        'Silent warning'),
        'ORDER_COMPLETED':  ('订单成交',        'Order completed'),
        'CHURN':            ('流失判定',        'Churn detected'),
        'SECOND_CARE':      ('二次关怀',        'Follow-up care'),
        'REACTIVE':         ('唤醒回访',        'Win-back outreach'),
        'ORDER_SHIPPED':    ('出库发货',        'Order shipped'),
        'ORDER_RETURNED':   ('退货',            'Order returned'),
        'PAYMENT_RECEIVED': ('收到回款',        'Payment received'),
        'DEBT_OVERDUE':     ('欠款逾期',        'Debt overdue'),
        'MEMBER_REGISTERED':('会员注册',        'Member registered'),
        'FIRST_PURCHASE':   ('首次消费',        'First purchase'),
        'POS_CHECKOUT':     ('收银结账',        'POS checkout'),
        'POS_REFUND':       ('收银退款',        'POS refund'),
        'MEMBER_RECHARGE':  ('会员充值',        'Member recharge'),
        'MEMBER_UPGRADE':   ('会员升级',        'Member upgraded'),
        'POINTS_CHANGED':   ('积分变动',        'Points changed'),
        'COMPLAINT':        ('投诉',            'Complaint'),
        'REFERRAL':         ('转介绍',          'Referral'),
        'CHURN_RISK':       ('流失预警',        'Churn risk'),
        'OTHER':            ('其他',            'Other'),
    },
    'INTENT': {
        'FIRST_CONTACT':  ('初次接触',   'First contact'),
        'INFO':           ('咨询了解',   'Seeking info'),
        'INTERESTED':     ('有意向',     'Interested'),
        'COMPARE':        ('比价',       'Comparing prices'),
        'SCHEMA_DISCUSS': ('方案讨论',   'Discussing proposal'),
        'QUOTE_GIVEN':    ('已报价',     'Quote given'),
        'SIGNED':         ('已签约',     'Signed'),
        'BUY':            ('已购买',     'Purchased'),
        'SILENT':         ('沉默',       'Gone quiet'),
        'RENEWED':        ('复购续约',   'Repeat purchase'),
        'CHURNED':        ('已流失',     'Churned'),
        'NURTURE':        ('培育中',     'Nurturing'),
        'COMPLAINT':      ('投诉不满',   'Complaint'),
        'CHITCHAT':       ('闲聊',       'Small talk'),
        'CHURN':          ('流失',       'Churn'),
        'BRONZE':         ('铜卡',       'Bronze'),
        'SILVER':         ('银卡',       'Silver'),
        'GOLD':           ('金卡',       'Gold'),
        'BLACK':          ('黑卡',       'Black'),
    },
    'CHANNEL': {
        'SYSTEM':   ('系统自动', 'System'),
        'WHATSAPP': ('WhatsApp', 'WhatsApp'),
        'PHONE':    ('电话',     'Phone'),
        'IN_STORE': ('门店',     'In store'),
        'FACEBOOK': ('Facebook', 'Facebook'),
        'EMAIL':    ('邮件',     'Email'),
        'OTHER':    ('其他',     'Other'),
    },
}


def column_exists(cur, table, column):
    cur.execute("""
        SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA=%s AND TABLE_NAME=%s AND COLUMN_NAME=%s
    """, (DB, table, column))
    return cur.fetchone()[0] > 0


def main():
    conn = pymysql.connect(host=HOST, port=PORT, user=USER, password=PW,
                           database=DB, charset='utf8mb4', autocommit=False)
    cur = conn.cursor()

    # ---- 1. 删掉重复字典表 ----
    cur.execute("DROP TABLE IF EXISTS touchpoint_type_dict")
    print('[OK] 已删除重复表 touchpoint_type_dict（字典统一走 journey_dict）')

    # ---- 2. 英文名列 ----
    if not column_exists(cur, 'journey_dict', 'dict_name_en'):
        cur.execute("""
            ALTER TABLE journey_dict
            ADD COLUMN dict_name_en VARCHAR(100) NULL COMMENT '英文名，配置页英文环境显示'
            AFTER dict_name
        """)
        print('[OK] journey_dict 增加 dict_name_en')
    else:
        print('[SKIP] dict_name_en 已存在')

    # ---- 3. 补齐中英文名（顺带覆盖掉乱码） ----
    updated, inserted = 0, 0
    for dict_type, items in DICTS.items():
        for code, (zh, en) in items.items():
            cur.execute("""
                SELECT id FROM journey_dict
                WHERE dict_type=%s AND dict_code=%s AND del_flag=0 LIMIT 1
            """, (dict_type, code))
            row = cur.fetchone()
            if row:
                cur.execute("""
                    UPDATE journey_dict SET dict_name=%s, dict_name_en=%s, update_time=NOW()
                    WHERE id=%s
                """, (zh, en, row[0]))
                updated += 1
            else:
                cur.execute("""
                    INSERT INTO journey_dict
                        (tenant_id, dict_type, dict_code, dict_name, dict_name_en,
                         subject_type, built_in, sort_order, enabled, del_flag,
                         create_time, update_time)
                    VALUES (NULL, %s, %s, %s, %s, 'BOTH', 1, 900, 1, 0, NOW(), NOW())
                """, (dict_type, code, zh, en))
                inserted += 1
    print(f'[OK] 字典补齐：更新 {updated} 条，新增 {inserted} 条')

    # ---- 4. 兜底：还有别处的双重编码一并修掉 ----
    cur.execute("""
        UPDATE journey_dict
        SET dict_name = CONVERT(BINARY(CONVERT(dict_name USING latin1)) USING utf8mb4)
        WHERE del_flag=0 AND (HEX(dict_name) LIKE '%C3A4%' OR HEX(dict_name) LIKE '%C3A5%'
           OR HEX(dict_name) LIKE '%C3A6%' OR HEX(dict_name) LIKE '%C3A7%'
           OR HEX(dict_name) LIKE '%C3A8%' OR HEX(dict_name) LIKE '%C3A9%')
    """)
    if cur.rowcount:
        print(f'[OK] 兜底修复 {cur.rowcount} 条乱码')
    else:
        print('[OK] 无残留乱码')

    conn.commit()

    print('\n===== 校验 =====')
    cur.execute("""
        SELECT dict_type, COUNT(*), SUM(dict_name_en IS NOT NULL)
        FROM journey_dict WHERE del_flag=0 GROUP BY dict_type
    """)
    for t, total, with_en in cur.fetchall():
        print(f'  {t:<18} 共 {total:>3} 条，已译 {with_en} 条')

    cur.execute("""
        SELECT dict_code, dict_name, dict_name_en FROM journey_dict
        WHERE dict_type='TOUCHPOINT_TYPE' AND del_flag=0
          AND dict_code IN ('SILENT_WARNING','CHURN','SECOND_CARE','REACTIVE')
    """)
    print('  原乱码项现状：')
    for c, zh, en in cur.fetchall():
        print(f'    {c:<18} {zh:<10} / {en}')

    cur.close()
    conn.close()
    print('\n迁移完成。')


if __name__ == '__main__':
    main()
