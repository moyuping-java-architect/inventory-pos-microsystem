package com.psi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.system.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 * 
 * @author PSI
 * @version 1.0.0
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}