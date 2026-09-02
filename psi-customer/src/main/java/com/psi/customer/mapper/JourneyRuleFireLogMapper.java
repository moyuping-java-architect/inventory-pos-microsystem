package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.JourneyRuleFireLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 规则触发幂等记录 Mapper。靠唯一索引挡住重复触发。
 *
 * @author PSI
 */
@Mapper
public interface JourneyRuleFireLogMapper extends BaseMapper<JourneyRuleFireLogEntity> {
}
