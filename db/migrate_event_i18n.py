# -*- coding: utf-8 -*-
"""
事件国际化 + 阶段关联 迁移（幂等）。
- business_event_dict 增加 event_name_en（英文名，老板可读）
- touchpoint_generate_rule 增加 stage_code（事件→阶段关联，零代码）
- 修正全部 26 个事件的 中/英文名（JOURNEY.* 之前是双重编码，一并修掉）
"""
import pymysql

HOST, PORT, USER, PW, DB = '127.0.0.1', 3307, 'root', 'Psi@Db#2026#Root', 'psi_modular'

# event_code -> (中文名, 英文名)
EVENTS = {
    'SALE.ORDER_SUBMITTED':        ('销售订单提交送审',   'Sales order submitted for approval'),
    'SALE.ORDER_APPROVED':         ('销售订单审批通过',   'Sales order approved'),
    'SALE.ORDER_REJECTED':         ('销售订单审批驳回',   'Sales order rejected'),
    'SALE.ORDER_SHIPPED':          ('销售出库发货',       'Sales order shipped'),
    'SALE.ORDER_RETURNED':         ('销售退货',           'Sales order returned'),
    'FINANCE.PAYMENT_RECEIVED':    ('客户付款登记',       'Customer payment received'),
    'FINANCE.DEBT_CHANGED':        ('欠款状态变更',       'Outstanding debt changed'),
    'FINANCE.CREDIT_EXCEEDED':     ('超出信用额度',       'Credit limit exceeded'),
    'FINANCE.RECEIVABLE_OVERDUE':  ('应收账款逾期',       'Receivable overdue'),
    'CUSTOMER.CREATED':            ('客户建档',           'Customer profile created'),
    'CUSTOMER.UPDATED':            ('客户信息变更',       'Customer info updated'),
    'CUSTOMER.LEVEL_CHANGED':      ('客户等级变更',       'Customer level changed'),
    'CUSTOMER.STATUS_CHANGED':     ('客户停用启用',       'Customer activated or suspended'),
    'CUSTOMER.CONVERTED_TO_MEMBER':('客户转为会员',       'Customer converted to member'),
    'CASHIER.CHECKOUT_COMPLETED':  ('收银结账完成',       'Checkout completed'),
    'CASHIER.REFUNDED':            ('收银退款',           'Refund issued'),
    'CASHIER.FULL_REFUNDED':       ('整单退完',          'Full order refunded'),
    'CASHIER.ORDER_HELD':          ('挂单',              'Order held (parked)'),
    'CASHIER.ORDER_RESUMED':       ('取单',              'Order resumed (un-parked)'),
    'MEMBER.REGISTERED':           ('会员注册',          'Member registered'),
    'MEMBER.RECHARGED':            ('会员充值',          'Member recharge (top-up)'),
    'MEMBER.CARD_CONSUMED':        ('会员卡消费',        'Member card consumed'),
    'MEMBER.POINTS_CHANGED':       ('积分增减',          'Member points changed'),
    'MEMBER.LEVEL_UPGRADED':       ('会员等级升级',      'Member level upgraded'),
    'JOURNEY.MEMBER_SILENT':       ('会员沉默预警(系统合成)', 'Member went silent (system)'),
    'JOURNEY.MEMBER_CHURNED':      ('会员流失判定(系统合成)', 'Member churned (system)'),
}

conn = pymysql.connect(host=HOST, port=PORT, user=USER, password=PW, database=DB, charset='utf8mb4')
cur = conn.cursor()


def col_exists(table, col):
    cur.execute(
        "SELECT 1 FROM information_schema.COLUMNS "
        "WHERE TABLE_SCHEMA=%s AND TABLE_NAME=%s AND COLUMN_NAME=%s",
        (DB, table, col))
    return cur.fetchone() is not None


# 1) 加列
if not col_exists('business_event_dict', 'event_name_en'):
    cur.execute("ALTER TABLE business_event_dict ADD COLUMN event_name_en VARCHAR(255) "
                "DEFAULT NULL COMMENT '事件英文名（国际化）'")
    print('+ business_event_dict.event_name_en')
else:
    print('= business_event_dict.event_name_en 已存在')

if not col_exists('touchpoint_generate_rule', 'stage_code'):
    cur.execute("ALTER TABLE touchpoint_generate_rule ADD COLUMN stage_code VARCHAR(64) "
                "DEFAULT NULL COMMENT '关联的客户旅程阶段编码（零代码事件→阶段映射）'")
    print('+ touchpoint_generate_rule.stage_code')
else:
    print('= touchpoint_generate_rule.stage_code 已存在')

# 2) 修正 26 事件名
updated = 0
for code, (zh, en) in EVENTS.items():
    cur.execute(
        "UPDATE business_event_dict SET event_name=%s, event_name_en=%s WHERE event_code=%s",
        (zh, en, code))
    if cur.rowcount > 0:
        updated += 1
        print(f'  {code:28s} <- {zh} / {en}')
print(f'\n事件名更新: {updated} 行')

conn.commit()
conn.close()
print('迁移完成。')
