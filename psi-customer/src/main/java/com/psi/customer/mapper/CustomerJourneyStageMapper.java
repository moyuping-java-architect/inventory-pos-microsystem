package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.CustomerJourneyStageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户旅程阶段 Mapper。阶段归属于某套旅程，按 sortOrder 排序决定先后。
 *
 * @author PSI
 */
@Mapper
public interface CustomerJourneyStageMapper extends BaseMapper<CustomerJourneyStageEntity> {

    @Select("SELECT * FROM customer_journey_stage WHERE stage_code = #{stageCode} LIMIT 1")
    CustomerJourneyStageEntity selectByStageCode(@Param("stageCode") String stageCode);
}
