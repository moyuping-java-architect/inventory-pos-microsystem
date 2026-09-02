package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.CustomerJourneyTemplateEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户旅程模板 Mapper。支持多套旅程并行，通过 journeyCode 区分。
 *
 * @author PSI
 */
@Mapper
public interface CustomerJourneyTemplateMapper extends BaseMapper<CustomerJourneyTemplateEntity> {
}
