package com.psi.cashier.service;

import com.psi.cashier.entity.OrderPendingItemEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 挂单明细服务接口
 * 提供挂单明细的数据操作
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface OrderPendingItemService extends IService<OrderPendingItemEntity> {

    void saveBatch(List<OrderPendingItemEntity> items);

    List<OrderPendingItemEntity> getByPendingNo(String pendingNo);

    void deleteByPendingNo(String pendingNo);
}