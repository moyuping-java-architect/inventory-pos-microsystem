package com.psi.cashier.service;

import com.psi.cashier.entity.OrderPayEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OrderPayService extends IService<OrderPayEntity> {

    List<OrderPayEntity> getByOrderNo(String orderNo);
}