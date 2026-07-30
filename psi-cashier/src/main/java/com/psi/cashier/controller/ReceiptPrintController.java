package com.psi.cashier.controller;

import com.psi.cashier.dto.CashierMainSaveDTO;
import com.psi.cashier.dto.RefundMainSaveDTO;
import com.psi.cashier.service.ReceiptPrintService;
import com.psi.common.result.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小票打印控制器
 * 提供小票打印相关的REST API接口
 * 注：小票打印已在收银/退款保存成功后自动触发，
 * 本控制器主要用于手动补打和打印机状态检查
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/psi/cashier/receipt")
public class ReceiptPrintController {

    private final ReceiptPrintService receiptPrintService;

    public ReceiptPrintController(ReceiptPrintService receiptPrintService) {
        this.receiptPrintService = receiptPrintService;
    }

    /**
     * 手动补打小票（同步）
     * 传入完整的收银DTO数据，直接拼接打印，不查询数据库
     * 
     * @param orderNo 订单号
     * @param dto     收银保存DTO
     * @return 操作结果
     */
    @PostMapping("/print/{orderNo}")
    public CommonResult<String> print(@PathVariable String orderNo,
                                      @RequestBody CashierMainSaveDTO dto) {
        try {
            receiptPrintService.print(dto, orderNo);
            return CommonResult.success("打印成功");
        } catch (Exception e) {
            log.error("打印小票失败，订单号：{}", orderNo, e);
            return CommonResult.fail("打印失败：" + e.getMessage());
        }
    }

    /**
     * 手动补打小票（异步）
     * 
     * @param orderNo 订单号
     * @param dto     收银保存DTO
     * @return 操作结果
     */
    @PostMapping("/print/async/{orderNo}")
    public CommonResult<String> printAsync(@PathVariable String orderNo,
                                           @RequestBody CashierMainSaveDTO dto) {
        try {
            receiptPrintService.printAsync(dto, orderNo);
            return CommonResult.success("打印任务已触发，请等待打印完成");
        } catch (Exception e) {
            log.error("触发异步打印失败，订单号：{}", orderNo, e);
            return CommonResult.fail("触发打印失败：" + e.getMessage());
        }
    }

    /**
     * 手动补打退款小票
     * 传入完整的退款DTO数据，直接拼接打印，不查询数据库
     * 
     * @param refundNo 退款单号
     * @param dto      退款保存DTO
     * @return 操作结果
     */
    @PostMapping("/print/refund/{refundNo}")
    public CommonResult<String> printRefund(@PathVariable String refundNo,
                                            @RequestBody RefundMainSaveDTO dto) {
        try {
            receiptPrintService.printRefund(dto, refundNo);
            return CommonResult.success("退款小票打印成功");
        } catch (Exception e) {
            log.error("打印退款小票失败，退款单号：{}", refundNo, e);
            return CommonResult.fail("打印失败：" + e.getMessage());
        }
    }

    /**
     * 检查打印机状态
     * 
     * @return true表示打印机就绪，false表示未就绪
     */
    @GetMapping("/printer/status")
    public CommonResult<Boolean> checkPrinterStatus() {
        boolean ready = receiptPrintService.isPrinterReady();
        return CommonResult.success(ready);
    }
}
