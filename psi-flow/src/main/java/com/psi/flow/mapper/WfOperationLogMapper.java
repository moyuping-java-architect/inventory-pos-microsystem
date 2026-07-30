package com.psi.flow.mapper;

import com.psi.flow.entity.WfOperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface WfOperationLogMapper extends BaseMapper<WfOperationLog> {
}