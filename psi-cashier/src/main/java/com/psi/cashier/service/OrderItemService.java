package com.psi.cashier.service;

import com.psi.cashier.entity.OrderItemEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OrderItemService extends IService<OrderItemEntity> {

    List<OrderItemEntity> getByOrderNo(String orderNo);
}