package com.psi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.system.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志Mapper接口
 * 
 * @author PSI
 * @version 1.0.0
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {
}