package com.psi.cashier.service.impl;

import com.psi.cashier.entity.CashierShiftPayEntity;
import com.psi.cashier.mapper.CashierShiftPayMapper;
import com.psi.cashier.service.CashierShiftPayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 班次支付明细服务实现类
 */
@Service
public class CashierShiftPayServiceImpl extends ServiceImpl<CashierShiftPayMapper, CashierShiftPayEntity> implements CashierShiftPayService {
}