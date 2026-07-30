package com.psi.cashier.service;

import com.psi.cashier.entity.RefundPayEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 退款支付服务接口
 * 提供退款支付的数据操作
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface RefundPayService extends IService<RefundPayEntity> {

    void saveBatch(List<RefundPayEntity> pays);

    List<RefundPayEntity> getByRefundNo(String refundNo);

    void deleteByRefundNo(String refundNo);
}