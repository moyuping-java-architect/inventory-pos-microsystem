package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.BusinessEventDictEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 业务事件字典 Mapper。系统业务动作的穷举清单。
 *
 * @author PSI
 */
@Mapper
public interface BusinessEventDictMapper extends BaseMapper<BusinessEventDictEntity> {
}
