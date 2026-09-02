package com.psi.cashier.controller;

import com.psi.cashier.dto.SettlementCreateDTO;
import com.psi.cashier.entity.CashierSettlementEntity;
import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.entity.OrderPayEntity;
import com.psi.cashier.service.CashierSettlementService;
import com.psi.cashier.service.OrderMainService;
import com.psi.cashier.service.OrderPayService;
import com.psi.cashier.service.SettlementCheckService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 日结控制器
 * 提供日结单的REST API接口
 * 
 * @author PSI
 * @version 1.0.0
 */
@RestController
@RequestMapping("/psi/cashier/settlement")
public class SettlementController {

    private final CashierSettlementService cashierSettlementService;
    private final OrderMainService orderMainService;
    private final OrderPayService orderPayService;
    private final SettlementCheckService settlementCheckService;

    public SettlementController(CashierSettlementService cashierSettlementService,
                               OrderMainService orderMainService,
                               OrderPayService orderPayService,
                               SettlementCheckService settlementCheckService) {
        this.cashierSettlementService = cashierSettlementService;
        this.orderMainService = orderMainService;
        this.orderPayService = orderPayService;
        this.settlementCheckService = settlementCheckService;
    }

    /**
     * 检查日结状态
     * 返回是否可以销售以及未日结的日期
     */
    @GetMapping("/check")
    public CommonResult<Map<String, Object>> checkSettlementStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("canSell", settlementCheckService.canSell());
        result.put("unsettledDate", settlementCheckService.getUnsettledDateStr());
        return CommonResult.success(result);
    }

    @GetMapping("/page")
    public PageResult<CashierSettlementEntity> queryPage(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                         @RequestParam(required = false) Integer operatorId,
                                                         @RequestParam(required = false) Integer status) {
        return cashierSettlementService.queryPage(pageNum, pageSize, operatorId, status);
    }

    @GetMapping("/{settleNo}")
    public CommonResult<CashierSettlementEntity> getBySettleNo(@PathVariable String settleNo) {
        CashierSettlementEntity settlement = cashierSettlementService.getBySettleNo(settleNo);
        if (settlement == null) {
            return CommonResult.fail("日结单不存在");
        }
        return CommonResult.success(settlement);
    }

    @GetMapping("/operator/{operatorId}")
    public CommonResult<List<CashierSettlementEntity>> getByOperatorId(@PathVariable Integer operatorId) {
        List<CashierSettlementEntity> settlements = cashierSettlementService.getByOperatorId(operatorId);
        return CommonResult.success(settlements);
    }

    @GetMapping("/date/{dateStr}")
    public CommonResult<Map<String, Object>> getByDate(@PathVariable String dateStr) {
        Map<String, Object> result = cashierSettlementService.getSettlementSummary(dateStr);
        return CommonResult.success(result);
    }

    @PostMapping
    public CommonResult<CashierSettlementEntity> create(@RequestBody SettlementCreateDTO dto) {
        // 先检查是否为空日结
        if (!settlementCheckService.canSettleToday(dto.getSettleDate())) {
            return CommonResult.fail("当日没有订单，不允许空日结");
        }
        
        CashierSettlementEntity saved = cashierSettlementService.createSettlement(dto.getSettleDate(), dto.getOperatorId());
        if (saved == null) {
            return CommonResult.fail("今日已经日结，无需再次日结");
        }
        return CommonResult.success(saved);
    }

    @PutMapping("/{settleNo}")
    public CommonResult<CashierSettlementEntity> update(@PathVariable String settleNo, @RequestBody CashierSettlementEntity settlement) {
        CashierSettlementEntity existing = cashierSettlementService.getBySettleNo(settleNo);
        if (existing == null) {
            return CommonResult.fail("日结单不存在");
        }
        settlement.setId(existing.getId());
        settlement.setSettleNo(settleNo);
        cashierSettlementService.update(settlement);
        return CommonResult.success(settlement);
    }

    @PutMapping("/{settleNo}/confirm")
    public CommonResult<Void> confirmSettlement(@PathVariable String settleNo) {
        boolean confirmed = cashierSettlementService.confirmSettlement(settleNo);
        if (!confirmed) {
            return CommonResult.fail("日结单不存在");
        }
        return CommonResult.success(null);
    }
}