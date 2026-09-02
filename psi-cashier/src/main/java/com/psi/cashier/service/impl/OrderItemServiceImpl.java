package com.psi.cashier.service.impl;

import com.psi.cashier.entity.OrderItemEntity;
import com.psi.cashier.mapper.OrderItemMapper;
import com.psi.cashier.service.OrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemEntity> implements OrderItemService {

    @Override
    public List<OrderItemEntity> getByOrderNo(String orderNo) {
        return baseMapper.selectByOrderNo(orderNo);
    }
}