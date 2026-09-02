package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.CustomerJourneyConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Mapper for customer_journey_config table.
 * Reads are far more frequent than writes (config cached in service layer).
 */
@Mapper
public interface CustomerJourneyConfigMapper extends BaseMapper<CustomerJourneyConfigEntity> {

    /**
     * Get all active config rows ordered by group then sort_order.
     */
    @Select("SELECT * FROM customer_journey_config WHERE del_flag = 0 ORDER BY config_group, sort_order")
    List<CustomerJourneyConfigEntity> selectAllActive();

    /**
     * Get all config rows for a specific group.
     */
    @Select("SELECT * FROM customer_journey_config WHERE del_flag = 0 AND config_group = #{group} ORDER BY sort_order")
    List<CustomerJourneyConfigEntity> selectByGroup(String group);

    /**
     * Get a single config row by group + key.
     */
    @Select("SELECT * FROM customer_journey_config WHERE del_flag = 0 AND config_group = #{group} AND config_key = #{key}")
    CustomerJourneyConfigEntity selectByGroupAndKey(String group, String key);
}
