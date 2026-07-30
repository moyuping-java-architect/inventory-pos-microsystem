# -*- coding: utf-8 -*-
import subprocess
import os

# Execute schema files
schemas = {
    'psi_flow': r"E:\spring boot\psi-parent\psi-flow\src\main\resources\schema.sql",
    'finance_db': r"E:\spring boot\psi-parent\psi-finance\src\main\resources\schema.sql",
}

# These have garbled Chinese comments - strip all COMMENT clauses
garbled_schemas = {
    'psi_stock': r"E:\spring boot\psi-parent\psi-stock\src\main\resources\schema.sql",
    'psi_purchase': r"E:\spring boot\psi-parent\psi-purchase\src\main\resources\schema.sql",
    'psi_sale': r"E:\spring boot\psi-parent\psi-sale\src\main\resources\schema.sql",
}

# First, run the clean schemas
for db, path in schemas.items():
    print(f"\n--- Creating tables for {db} ---")
    with open(path, 'rb') as f:
        raw = f.read()
    if raw.startswith(b'\xef\xbb\xbf'):
        raw = raw[3:]
    
    sql = b"USE `" + db.encode() + b"`;\n" + raw
    
    proc = subprocess.Popen(
        ['mysql', '-u', 'root', '-p123456', '--default-character-set=utf8'],
        stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE
    )
    out, err = proc.communicate(sql)
    
    if err and err.strip():
        err_text = err.decode('utf-8', errors='replace')
        errors = [l for l in err_text.split('\n') if 'Warning' not in l and l.strip()]
        for e in errors[-2:]:
            print(f"  Error: {e[:150]}")
    else:
        print(f"  Success!")

# Now fix and run garbled schemas
# Strategy: just recreate the files with clean COMMENT clauses
for db, path in garbled_schemas.items():
    print(f"\n--- Creating tables for {db} (clean comments) ---")
    with open(path, 'rb') as f:
        raw = f.read()
    if raw.startswith(b'\xef\xbb\xbf'):
        raw = raw[3:]
    
    # Approach: split into lines, for each line check if it has COMMENT
    # If it does, replace the entire COMMENT clause with empty string
    lines = raw.split(b'\n')
    cleaned_lines = []
    for line in lines:
        # Check if line has COMMENT
        ci = line.find(b'COMMENT')
        if ci >= 0:
            # Find the next delimiter (, or ;) after COMMENT - NOT ) because garbled text may contain )
            rest = line[ci:]
            # Find delimiter
            delim_pos = -1
            for ch in b',;':
                p = rest.find(bytes([ch]))
                if p != -1 and (delim_pos == -1 or p < delim_pos):
                    delim_pos = p
            
            if delim_pos >= 0:
                # Keep: text before COMMENT (minus trailing space) + delimiter
                before = line[:ci].rstrip()
                delim = bytes([rest[delim_pos]])
                cleaned_lines.append(before + delim)
            else:
                # No delimiter - just keep text before COMMENT
                cleaned_lines.append(line[:ci].rstrip())
        else:
            cleaned_lines.append(line)
    
    cleaned = b'\n'.join(cleaned_lines)
    
    # Verify: check if any line still has garbled COMMENT content
    # by counting that we removed all COMMENTS
    remaining_comments = cleaned.count(b'COMMENT')
    if remaining_comments > 0:
        print(f"  Warning: {remaining_comments} COMMENT clauses remaining!")
    
    sql = b"USE `" + db.encode() + b"`;\n" + cleaned
    
    proc = subprocess.Popen(
        ['mysql', '-u', 'root', '-p123456', '--default-character-set=utf8'],
        stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE
    )
    out, err = proc.communicate(sql)
    
    if err and err.strip():
        err_text = err.decode('utf-8', errors='replace')
        errors = [l for l in err_text.split('\n') if 'Warning' not in l and l.strip()]
        for e in errors[-2:]:
            print(f"  Error: {e[:150]}")
    else:
        print(f"  Success!")

# Final verification
print(f"\n{'='*50}")
print("Final Table Count:")
result = subprocess.run(
    ['mysql', '-u', 'root', '-p123456',
     '-e', "SELECT TABLE_SCHEMA, COUNT(*) as cnt FROM information_schema.tables "
           "WHERE table_schema IN ('psi_stock','psi_purchase','psi_sale','psi_flow','finance_db',"
           "'psi_goods','psi_message','erp_system_db','psi_sync_db') "
           "GROUP BY TABLE_SCHEMA ORDER BY TABLE_SCHEMA;"],
    capture_output=True
)
for line in result.stdout.decode('utf-8', errors='replace').split('\n'):
    if line.strip():
        print(f"  {line.strip()}")
print("\nDone!")