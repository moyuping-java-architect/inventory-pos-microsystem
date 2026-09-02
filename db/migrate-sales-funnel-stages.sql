-- Migrate existing SALES_FUNNEL stage configs from flat JOURNEY_STAGES group
-- to the new namespaced JOURNEY_STAGE_CONFIG group so the Config page UI
-- shows the same color/icon/matchTouchpoint/matchIntent/tip values
-- without forcing the user to re-save them.
--
-- Safe to re-run: existing namespaced rows are deleted first, then re-inserted
-- from the current source of truth (JOURNEY_STAGES).
--
-- After this, the engine continues to read JOURNEY_STAGES (unchanged), and
-- the UI Config page reads/writes JOURNEY_STAGE_CONFIG (new surface).
-- Future enhancement: switch the engine to read JOURNEY_STAGE_CONFIG.

DELETE FROM customer_journey_config
WHERE config_group = 'JOURNEY_STAGE_CONFIG'
  AND config_key LIKE 'SALES_FUNNEL::%';

INSERT INTO customer_journey_config
    (config_group, config_key, config_value, value_type, display_name, description, sort_order, del_flag, create_time, update_time)
SELECT
    'JOURNEY_STAGE_CONFIG',
    CONCAT('SALES_FUNNEL::', config_key),
    config_value,
    'OPTION',
    display_name,
    description,
    sort_order,
    0,
    NOW(),
    NOW()
FROM customer_journey_config
WHERE config_group = 'JOURNEY_STAGES'
  AND config_key LIKE 'STAGE_%';

-- Verify
SELECT config_key, LEFT(config_value, 60) AS val_preview
FROM customer_journey_config
WHERE config_group = 'JOURNEY_STAGE_CONFIG'
ORDER BY config_key;