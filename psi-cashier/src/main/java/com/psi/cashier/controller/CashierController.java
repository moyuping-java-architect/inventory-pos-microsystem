package com.psi.cashier.controller;

import com.psi.cashier.dto.CashierBatchDTO;
import com.psi.cashier.dto.CashierMainSaveDTO;
import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.feign.StockBatchFeignClient;
import com.psi.cashier.mq.producer.CashierSyncProducer;
import com.psi.cashier.service.CashierService;
import com.psi.common.result.CommonResult;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收银控制器
 * 提供收银订单的REST API接口
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/psi/cashier")
public class CashierController {

    private final CashierService cashierService;
    private final CashierSyncProducer cashierSyncProducer;
    private final StockBatchFeignClient stockBatchFeignClient;

    public CashierController(CashierService cashierService,
                             CashierSyncProducer cashierSyncProducer,
                             StockBatchFeignClient stockBatchFeignClient) {
        this.cashierService = cashierService;
        this.cashierSyncProducer = cashierSyncProducer;
        this.stockBatchFeignClient = stockBatchFeignClient;
    }

    @PostMapping("/save")
    public CommonResult<OrderMainEntity> saveOrder(@Valid @RequestBody CashierMainSaveDTO dto) {
        OrderMainEntity order = cashierService.saveOrder(dto);
        log.info("订单保存成功，订单号：{}", order.getOrderNo());
        return CommonResult.success(order);
    }

    /**
     * 健康检查接口，供前端检测网络连通性
     */
    @GetMapping("/health")
    public CommonResult<String> health() {
        return CommonResult.success("ok");
    }

    /**
     * 批量同步离线订单
     */
    @PostMapping("/offline-sync")
    public CommonResult<Map<String, Object>> offlineSync(@Valid @RequestBody List<CashierMainSaveDTO> orders) {
        int successCount = 0;
        int failCount = 0;
        List<String> failedOrderNos = new ArrayList<>();

        for (CashierMainSaveDTO dto : orders) {
            try {
                cashierService.saveOrder(dto);
                successCount++;
            } catch (Exception e) {
                failCount++;
                String orderNo = dto.getCashierNo() != null ? dto.getCashierNo() : "unknown";
                failedOrderNos.add(orderNo);
                log.error("离线订单同步失败，单号：{}", orderNo, e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failedOrderNos", failedOrderNos);
        return CommonResult.success(result);
    }

    /**
     * 根据商品编码查询有效批次
     */
    @GetMapping("/batch/{goodsCode}")
    public CommonResult<List<CashierBatchDTO>> listBatchByGoodsCode(@PathVariable String goodsCode) {
        CommonResult<List<CashierBatchDTO>> result = stockBatchFeignClient.listValidByGoodsCode(goodsCode);
        if (result == null || result.getData() == null) {
            return CommonResult.success(new ArrayList<>());
        }
        return result;
    }

    /**
     * 手动触发数据上传
     * 适用于无网络环境下先离线存储，联网后手动触发上传
     * 
     * @return 操作结果
     */
    @PostMapping("/upload")
    public CommonResult<String> uploadData() {
        cashierSyncProducer.syncAllAsync();
        return CommonResult.success("数据上传任务已触发，请稍后查看日志确认上传结果");
    }
}
