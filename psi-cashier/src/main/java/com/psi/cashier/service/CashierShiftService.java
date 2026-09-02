package com.psi.cashier.service;

import com.psi.cashier.entity.CashierShiftEntity;
import com.psi.cashier.entity.CashierShiftPayEntity;
import com.psi.common.result.PageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 班次结算服务接口
 * 提供班次结算单的CRUD操作及相关统计功能
 */
public interface CashierShiftService {

    /**
     * 保存班次结算
     */
    CashierShiftEntity save(CashierShiftEntity entity);

    /**
     * 根据班次单号查询
     */
    CashierShiftEntity getByShiftNo(String shiftNo);

    /**
     * 根据收银员ID查询班次记录
     */
    List<CashierShiftEntity> getByOperatorId(Integer operatorId);

    /**
     * 根据日期查询班次记录
     */
    List<CashierShiftEntity> getByDate(String dateStr);

    /**
     * 分页查询班次记录
     */
    PageResult<CashierShiftEntity> queryPage(int pageNum, int pageSize, Integer operatorId, Integer status);

    /**
     * 更新班次结算
     */
    boolean update(CashierShiftEntity entity);

    /**
     * 确认班次结算
     */
    boolean confirmShift(String shiftNo);

    /**
     * 删除班次结算
     */
    boolean delete(String shiftNo);

    /**
     * 计算班次统计数据
     */
    Map<String, Object> calculateShiftData(Integer operatorId, String beginTime, String endTime);

    /**
     * 创建班次结算（下班结账）
     */
    CashierShiftEntity createShift(Integer operatorId, String operatorName, BigDecimal cashBegin);

    /**
     * 获取班次支付明细
     */
    List<CashierShiftPayEntity> getShiftPayList(String shiftNo);

    /**
     * 保存班次支付明细
     */
    void saveShiftPayList(String shiftNo, List<CashierShiftPayEntity> payList);

    /**
     * 检查收银员当天是否已有未完成的班次
     */
    boolean hasUnfinishedShift(Integer operatorId);

    /**
     * 获取收银员最新的班次记录
     */
    CashierShiftEntity getLastShift(Integer operatorId);
}