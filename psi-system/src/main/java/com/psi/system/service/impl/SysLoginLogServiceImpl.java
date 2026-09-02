package com.psi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.system.entity.SysLoginLog;
import com.psi.system.mapper.SysLoginLogMapper;
import com.psi.system.service.SysLoginLogService;
import org.springframework.stereotype.Service;

/**
 * 登录日志Service实现类
 * 
 * @author PSI
 * @version 1.0.0
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {
}