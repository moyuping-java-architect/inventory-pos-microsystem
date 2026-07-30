package com.psi.cashier.service;

import com.psi.cashier.entity.OrderMainEntity;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OrderMainService extends IService<OrderMainEntity> {

    OrderMainEntity getByOrderNo(String orderNo);

    PageResult<OrderMainEntity> queryPage(int pageNum, int pageSize, Integer payStatus);

    PageResult<OrderMainEntity> queryOrders(int pageNum, int pageSize, String orderNo, String date);
}