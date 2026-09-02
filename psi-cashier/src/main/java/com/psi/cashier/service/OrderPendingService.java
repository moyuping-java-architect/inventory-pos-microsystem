package com.psi.cashier.service;

import com.psi.cashier.dto.PendingItemSaveDTO;
import com.psi.cashier.dto.PendingMainSaveDTO;
import com.psi.cashier.entity.OrderPendingEntity;
import com.psi.common.result.PageResult;

import java.util.List;

public interface OrderPendingService {

    OrderPendingEntity savePending(PendingMainSaveDTO dto);

    PendingMainSaveDTO getPending(Long id);

    PendingMainSaveDTO getPendingByNo(String pendingNo);

    List<PendingMainSaveDTO> listByOperatorId(Integer operatorId);

    List<PendingMainSaveDTO> listByShopCode(String shopCode);

    boolean deletePending(Long id);

    PageResult<OrderPendingEntity> queryPage(int pageNum, int pageSize, Integer operatorId);

    List<OrderPendingEntity> getAllPending();

    OrderPendingEntity getByPendingNo(String pendingNo);

    List<OrderPendingEntity> getByOperatorId(Integer operatorId);

    OrderPendingEntity save(OrderPendingEntity entity);

    void update(OrderPendingEntity entity);

    boolean deleteByPendingNo(String pendingNo);
}