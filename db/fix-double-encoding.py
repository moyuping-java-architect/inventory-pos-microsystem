"""
⚠️⚠️⚠️  DO NOT RUN THIS SCRIPT  ⚠️⚠️⚠️

It CORRUPTS cleanly-stored columns by treating ALL latin1-OK bytes as double-encoded.

What it does wrong:
  - Reads EVERY string column through a latin1 client and treats all of them as if
    they were double-encoded. Clean rows (e.g. "销售漏斗" = 12 bytes / 4 chars) get
    re-encoded → result is `????` (4 x 0x3F) or worse.
  - The script "succeeds" because some columns are corrupted at the same time,
    but the clean ones are silently destroyed.

The CORRECT script for this case is `db/restore-template-names.py`:
  - Connects utf8mb4
  - Hardcodes the known-correct Chinese text for the 3 template name rows
  - Only touches the rows known to be polluted (verified via OCTET_LENGTH vs
    CHAR_LENGTH diagnostic).

This file is kept only as a reference for what NOT to do. See memory 2026-08-09.
"""

import pymysql  # noqa  (deps kept so the script stays syntactically valid)

Safe to run multiple times: only updates rows whose byte-length > char-count*3.
"""
import pymysql

DB = dict(host='127.0.0.1', port=3307, user='root',
          password='Psi@Db#2026#Root', database='psi_modular',
          charset='latin1')  # critical: bypass utf8mb4 transcoding

TABLES = [
    ('customer_journey_template', 'journey_name'),
    ('customer_journey_template', 'description'),
    ('customer_journey_template', 'icon'),
    ('customer_journey_stage',     'stage_name'),
    ('customer_journey_stage',     'tip'),
    ('customer_journey_stage',     'match_touchpoint'),
    ('customer_journey_stage',     'match_intent'),
]

conn = pymysql.connect(**DB)
cur = conn.cursor()

for tbl, col in TABLES:
    cur.execute(f"SELECT id, `{col}` FROM `{tbl}` WHERE `{col}` IS NOT NULL")
    rows = cur.fetchall()
    fixed_count = 0
    for rid, val in rows:
        # In latin1 client, each Python str codepoint is 0..255 → represents original byte
        try:
            raw_bytes = val.encode('latin1')        # original bytes
            fixed = raw_bytes.decode('utf-8')       # proper Unicode
        except (UnicodeDecodeError, UnicodeEncodeError):
            print(f"  skip {tbl}.{col} id={rid}: not double-encoded ({len(val)} chars)")
            continue
        print(f"  fix  {tbl}.{col} id={rid}: {val!r} -> {fixed!r}")
        cur.execute(f"UPDATE `{tbl}` SET `{col}`=%s WHERE id=%s", (fixed, rid))
        fixed_count += 1
    conn.commit()
    print(f"  -> {fixed_count} row(s) updated in {tbl}.{col}")

conn.close()
print("\nDone.")
