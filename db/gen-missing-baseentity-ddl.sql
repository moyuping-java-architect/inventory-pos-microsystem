-- 生成所有缺失 BaseEntity 列的 ALTER 语句
SELECT CONCAT('ALTER TABLE `', t.table_name, '` ADD COLUMN `', m.col, '` ',
       CASE m.col
         WHEN 'create_by' THEN 'BIGINT NULL COMMENT ''创建人'''
         WHEN 'update_by' THEN 'BIGINT NULL COMMENT ''更新人'''
         WHEN 'update_time' THEN 'DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'''
         WHEN 'status' THEN 'TINYINT NULL DEFAULT 1 COMMENT ''状态'''
       END, ';') AS ddl
FROM (
  SELECT table_name, 'create_by' AS col FROM information_schema.tables tb
    WHERE tb.table_schema='psi_modular' AND tb.table_type='BASE TABLE'
      AND NOT EXISTS (SELECT 1 FROM information_schema.columns c WHERE c.table_schema='psi_modular' AND c.table_name=tb.table_name AND c.column_name='create_by')
  UNION ALL SELECT table_name, 'update_by' FROM information_schema.tables tb
    WHERE tb.table_schema='psi_modular' AND tb.table_type='BASE TABLE'
      AND NOT EXISTS (SELECT 1 FROM information_schema.columns c WHERE c.table_schema='psi_modular' AND c.table_name=tb.table_name AND c.column_name='update_by')
  UNION ALL SELECT table_name, 'update_time' FROM information_schema.tables tb
    WHERE tb.table_schema='psi_modular' AND tb.table_type='BASE TABLE'
      AND NOT EXISTS (SELECT 1 FROM information_schema.columns c WHERE c.table_schema='psi_modular' AND c.table_name=tb.table_name AND c.column_name='update_time')
  UNION ALL SELECT table_name, 'status' FROM information_schema.tables tb
    WHERE tb.table_schema='psi_modular' AND tb.table_type='BASE TABLE'
      AND NOT EXISTS (SELECT 1 FROM information_schema.columns c WHERE c.table_schema='psi_modular' AND c.table_name=tb.table_name AND c.column_name='status')
) m
JOIN (SELECT table_name FROM information_schema.tables WHERE table_schema='psi_modular' AND table_type='BASE TABLE'
      AND (EXISTS (SELECT 1 FROM information_schema.columns c WHERE c.table_schema='psi_modular' AND c.table_name=information_schema.tables.table_name AND c.column_name='data_uuid')
        OR EXISTS (SELECT 1 FROM information_schema.columns c WHERE c.table_schema='psi_modular' AND c.table_name=information_schema.tables.table_name AND c.column_name='tenant_id')
        OR EXISTS (SELECT 1 FROM information_schema.columns c WHERE c.table_schema='psi_modular' AND c.table_name=information_schema.tables.table_name AND c.column_name='del_flag'))) t
  ON t.table_name = m.table_name
ORDER BY t.table_name, m.col;
