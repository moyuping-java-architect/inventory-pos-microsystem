package com.trademaster.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.workflow.entity.WfProcessInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfProcessInstanceMapper extends BaseMapper<WfProcessInstance> {
}
