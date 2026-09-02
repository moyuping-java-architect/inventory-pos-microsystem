SELECT config_key, config_value FROM customer_journey_config WHERE config_group='TOUCHPOINT_TYPE' AND del_flag=0;
SELECT config_key, config_value FROM customer_journey_config WHERE config_group='INTENT_TYPE' AND del_flag=0;
SELECT journey_code, stage_code, stage_name, match_touchpoint, match_intent FROM customer_journey_stage WHERE journey_code='SALES_FUNNEL' AND del_flag=0 ORDER BY sort_order;
