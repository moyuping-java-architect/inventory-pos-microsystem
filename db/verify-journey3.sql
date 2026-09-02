SELECT '--- touchpoints member_id=2 ---' AS hdr;
SELECT id, member_id, customer_id, touchpoint_type, channel, contact_time, LEFT(summary,60) AS summary, intent FROM customer_touchpoint WHERE member_id=2 ORDER BY id;
SELECT '--- journey states subject_id=2 ---' AS hdr;
SELECT id, journey_code, subject_type, subject_id, current_stage_code, current_stage_order, advance_count, last_event_code FROM customer_journey_state WHERE subject_id=2 ORDER BY id;
