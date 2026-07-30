package com.psi.cashier.service;

import com.psi.cashier.entity.RefundOrderEntity;
import com.psi.common.result.PageResult;

import java.util.List;

/**
 * 退货订单服务接口
 * 提供退货订单的CRUD操作
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface RefundOrderService {

    RefundOrderEntity save(RefundOrderEntity entity);

    RefundOrderEntity getByRefundNo(String refundNo);

    List<RefundOrderEntity> getBySourceOrderNo(String sourceOrderNo);

    List<RefundOrderEntity> getByOperatorId(Integer operatorId);

    PageResult<RefundOrderEntity> queryPage(int pageNum, int pageSize, String refundNo, Integer operatorId);

    boolean update(RefundOrderEntity entity);

    boolean deleteByRefundNo(String refundNo);

    boolean isOrderFullyRefunded(String sourceOrderNo);

    String checkItemRefundQuantity(String sourceOrderNo, List<com.psi.cashier.dto.RefundItemSaveDTO> refundItems);
}