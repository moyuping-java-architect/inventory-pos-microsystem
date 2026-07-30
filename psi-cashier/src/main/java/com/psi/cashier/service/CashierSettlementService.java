package com.psi.cashier.service;

import com.psi.cashier.entity.CashierSettlementEntity;
import com.psi.common.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 日结服务接口
 * 提供日结单的CRUD操作及确认功能
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface CashierSettlementService {

    CashierSettlementEntity save(CashierSettlementEntity entity);

    CashierSettlementEntity getBySettleNo(String settleNo);

    List<CashierSettlementEntity> getByOperatorId(Integer operatorId);

    List<CashierSettlementEntity> getByDate(String dateStr);

    PageResult<CashierSettlementEntity> queryPage(int pageNum, int pageSize, Integer operatorId, Integer status);

    boolean update(CashierSettlementEntity entity);

    boolean confirmSettlement(String settleNo);

    Map<String, Object> getSettlementSummary(String dateStr);

    CashierSettlementEntity createSettlement(String dateStr, Integer operatorId);
}