package com.psi.flow.mapper;

import com.psi.flow.entity.WfProcessInstanceBiz;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程业务关联Mapper
 */
@Mapper
public interface WfProcessInstanceBizMapper extends BaseMapper<WfProcessInstanceBiz> {
}