package com.psi.cashier.service;

import com.psi.cashier.dto.CashierMainSaveDTO;
import com.psi.cashier.entity.OrderMainEntity;

/**
 * 收银服务接口
 * 提供收银订单相关业务操作
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface CashierService {

    /**
     * 保存收银订单（同步打印小票）
     * 
     * @param dto 收银保存数据传输对象
     * @return 保存成功的主订单实体
     */
    OrderMainEntity saveOrder(CashierMainSaveDTO dto);

    /**
     * 保存收银订单
     * 
     * @param dto        收银保存数据传输对象
     * @param asyncPrint 是否异步打印小票
     * @return 保存成功的主订单实体
     */
    OrderMainEntity saveOrder(CashierMainSaveDTO dto, boolean asyncPrint);
}