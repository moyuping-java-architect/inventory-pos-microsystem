package com.psi.cashier.service;

import com.psi.cashier.entity.RefundOrderItemEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 退货明细服务接口
 * 提供退货明细的数据操作
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface RefundOrderItemService extends IService<RefundOrderItemEntity> {

    void saveBatch(List<RefundOrderItemEntity> items);

    List<RefundOrderItemEntity> getByRefundNo(String refundNo);

    void deleteByRefundNo(String refundNo);

    List<RefundOrderItemEntity> getBySourceOrderNo(String sourceOrderNo);
}