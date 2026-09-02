package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.CustomerTagRelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户标签关系 Mapper
 */
@Mapper
public interface CustomerTagRelMapper extends BaseMapper<CustomerTagRelEntity> {
}
