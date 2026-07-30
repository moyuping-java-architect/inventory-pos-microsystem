package com.trademaster.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.workflow.entity.WfTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfTaskMapper extends BaseMapper<WfTask> {
}
