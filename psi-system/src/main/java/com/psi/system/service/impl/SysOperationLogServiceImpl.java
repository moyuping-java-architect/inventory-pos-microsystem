package com.psi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psi.system.entity.SysOperationLog;
import com.psi.system.mapper.SysOperationLogMapper;
import com.psi.system.service.SysOperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志Service实现类
 * 
 * @author PSI
 * @version 1.0.0
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {
}