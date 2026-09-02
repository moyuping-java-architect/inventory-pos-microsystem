SELECT '--- member 2 info ---' AS hdr;
SELECT id, member_no, balance, points, total_consume, total_orders FROM member_info WHERE id=2;
SELECT '--- touchpoints (subject MEMBER id 2) ---' AS hdr;
SELECT ct.id, ct.subject_type, ct.subject_id, ct.event_code, ct.touchpoint_type, ct.intent, ct.biz_key, LEFT(ct.payload,90) AS payload FROM customer_touchpoint ct WHERE ct.subject_id=2 ORDER BY ct.id;
SELECT '--- journey states (subject 2) ---' AS hdr;
SELECT cjs.id, cjs.journey_code, cjs.current_stage_code, cjs.current_stage_name, cjs.status FROM customer_journey_state cjs WHERE cjs.subject_id=2 ORDER BY cjs.id;
SELECT '--- balance/point logs ---' AS hdr;
SELECT id, type, amount, before_balance, after_balance, source_no FROM member_balance_log WHERE member_id=2 ORDER BY id;
SELECT id, type, points, before_points, after_points, source_no FROM member_point_log WHERE member_id=2 ORDER BY id;
