package com.psi.cashier.service.impl;

import com.psi.cashier.entity.OperatorEntity;
import com.psi.cashier.mapper.OperatorMapper;
import com.psi.cashier.service.OperatorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 操作员服务实现
 */
@Slf4j
@Service
public class OperatorServiceImpl extends ServiceImpl<OperatorMapper, OperatorEntity> implements OperatorService {
}