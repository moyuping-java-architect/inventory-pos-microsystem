package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.JourneyDictEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户旅程字典 Mapper。配置页所有下拉框的数据源。
 *
 * @author PSI
 */
@Mapper
public interface JourneyDictMapper extends BaseMapper<JourneyDictEntity> {
}
