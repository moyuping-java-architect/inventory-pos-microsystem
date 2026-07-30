package com.trademaster.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.workflow.entity.WfOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfOperationLogMapper extends BaseMapper<WfOperationLog> {
}
