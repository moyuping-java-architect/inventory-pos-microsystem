# -*- coding: utf-8 -*-
"""
孤儿租户数据归位（幂等）。

现象：销售漏斗一条状态都建不出来，触点、阶段、规则却都对得上。
根因：早期 demo 数据把 tenant_id 写成了字符串 'default'，
而系统里真实存在的租户只有 id='1'。MyBatis 租户插件会给每条查询
自动追加 tenant_id 条件，于是这 6 条触点在应用层等于不存在——
库里看得见，代码里查不到，最难排的那类问题。

把 'default' 统一改成 '1'，并顺手修掉 sys_tenant 里双重编码的中文。
"""
import pymysql

HOST, PORT, USER, PW, DB = '127.0.0.1', 3307, 'root', 'Psi@Db#2026#Root', 'psi_modular'

ORPHAN_VALUE = 'default'


def main():
    conn = pymysql.connect(host=HOST, port=PORT, user=USER, password=PW,
                           database=DB, charset='utf8mb4', autocommit=False)
    cur = conn.cursor()

    # 真实租户：取 sys_tenant 里最小的启用租户
    cur.execute("SELECT id FROM sys_tenant WHERE del_flag=0 ORDER BY id LIMIT 1")
    row = cur.fetchone()
    if not row:
        print('[ABORT] sys_tenant 无有效租户，不敢乱改')
        return
    real_tenant = str(row[0])
    print(f'[INFO] 真实租户 = {real_tenant}')

    # 找出所有带 tenant_id 的表。
    # 只挑字符串列：有些表把 tenant_id 建成了数字类型，
    # 拿 'default' 去比会直接报 Truncated incorrect DOUBLE value，
    # 而且数字列本来也不可能存进 'default'。
    cur.execute("""
        SELECT TABLE_NAME FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA=%s AND COLUMN_NAME='tenant_id'
          AND DATA_TYPE IN ('varchar','char','text')
    """, (DB,))
    tables = [r[0] for r in cur.fetchall()]

    total = 0
    for t in tables:
        cur.execute(f"SELECT COUNT(*) FROM `{t}` WHERE tenant_id=%s", (ORPHAN_VALUE,))
        cnt = cur.fetchone()[0]
        if cnt == 0:
            continue
        cur.execute(f"UPDATE `{t}` SET tenant_id=%s WHERE tenant_id=%s",
                    (real_tenant, ORPHAN_VALUE))
        print(f"  {t:<28} {cnt} 条 '{ORPHAN_VALUE}' -> '{real_tenant}'")
        total += cnt
    print(f'[OK] 归位 {total} 条孤儿数据')

    # sys_tenant 中文双重编码
    cur.execute("""
        UPDATE sys_tenant SET
            tenant_name  = CONVERT(BINARY(CONVERT(tenant_name  USING latin1)) USING utf8mb4),
            contact_name = CONVERT(BINARY(CONVERT(contact_name USING latin1)) USING utf8mb4),
            address      = CONVERT(BINARY(CONVERT(address      USING latin1)) USING utf8mb4)
        WHERE HEX(tenant_name) LIKE '%C3A4%' OR HEX(tenant_name) LIKE '%C3A5%'
           OR HEX(tenant_name) LIKE '%C3A6%' OR HEX(tenant_name) LIKE '%C3A8%'
           OR HEX(tenant_name) LIKE '%C3A9%'
    """)
    if cur.rowcount:
        print(f'[OK] 修复 {cur.rowcount} 条租户名乱码')

    conn.commit()

    print('\n===== 校验 =====')
    cur.execute("SELECT id, tenant_name, contact_name FROM sys_tenant WHERE del_flag=0")
    for i, n, c in cur.fetchall():
        print(f'  租户 {i}: {n} / {c}')
    cur.execute("""
        SELECT tenant_id, COUNT(*) FROM customer_touchpoint
        WHERE del_flag=0 GROUP BY tenant_id
    """)
    print('  触点租户分布:', dict(cur.fetchall()))

    cur.close()
    conn.close()
    print('\n迁移完成。')


if __name__ == '__main__':
    main()
