package com.psi.cashier.service;

import com.psi.cashier.dto.CashierMainSaveDTO;
import com.psi.cashier.dto.RefundMainSaveDTO;

/**
 * 小票打印服务接口
 * 提供小票打印相关功能
 * 支持网络打印机和USB直连打印机两种模式
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface ReceiptPrintService {

    /**
     * 打印小票（从数据库查询数据）
     * 
     * @param orderNo 订单号
     */
    void print(String orderNo);

    /**
     * 打印小票（直接使用DTO数据，避免重复查询数据库）
     * 
     * @param dto     收银保存DTO
     * @param orderNo 订单号
     */
    void print(CashierMainSaveDTO dto, String orderNo);

    /**
     * 异步打印小票
     * 
     * @param dto     收银保存DTO
     * @param orderNo 订单号
     */
    void printAsync(CashierMainSaveDTO dto, String orderNo);

    /**
     * 检查打印机状态
     * 
     * @return true表示打印机就绪，false表示未就绪
     */
    boolean isPrinterReady();

    /**
     * 打印退款小票（从数据库查询数据）
     * 
     * @param refundNo 退款单号
     */
    void printRefund(String refundNo);

    /**
     * 打印退款小票（直接使用DTO数据，避免重复查询数据库）
     * 
     * @param dto      退款保存DTO
     * @param refundNo 退款单号
     */
    void printRefund(RefundMainSaveDTO dto, String refundNo);
}
