package com.psi.cashier.service.impl;

import com.psi.cashier.entity.OrderPayEntity;
import com.psi.cashier.mapper.OrderPayMapper;
import com.psi.cashier.service.OrderPayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderPayServiceImpl extends ServiceImpl<OrderPayMapper, OrderPayEntity> implements OrderPayService {

    @Override
    public List<OrderPayEntity> getByOrderNo(String orderNo) {
        return baseMapper.selectByOrderNo(orderNo);
    }
}