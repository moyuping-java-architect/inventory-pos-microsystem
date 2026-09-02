SELECT '=== MEMBERSHIP 旅程阶段 ===' AS hdr;
SELECT sort_order, stage_code, stage_name, match_touchpoint, match_intent, allow_rollback FROM customer_journey_stage WHERE journey_code='MEMBERSHIP' ORDER BY sort_order;
SELECT '=== MEMBER 触点规则 ===' AS hdr;
SELECT id, rule_name, event_code, condition_json, touchpoint_type, intent, once_only, sort_order, enabled FROM touchpoint_generate_rule WHERE event_code LIKE 'MEMBER.%' AND (tenant_id IS NULL OR tenant_id='1') ORDER BY sort_order;
SELECT '=== FINANCE 触点规则 ===' AS hdr;
SELECT id, rule_name, event_code, condition_json, touchpoint_type, intent, once_only, sort_order, enabled FROM touchpoint_generate_rule WHERE event_code LIKE 'FINANCE.%' AND (tenant_id IS NULL OR tenant_id='1') ORDER BY sort_order;
SELECT '=== business_event_dict 实现状态 ===' AS hdr;
SELECT event_code, event_name, implemented FROM business_event_dict WHERE event_code LIKE 'MEMBER.%' OR event_code LIKE 'FINANCE.%' ORDER BY event_code;
