package com.psi.cashier.controller;

import com.psi.cashier.entity.OrderPendingEntity;
import com.psi.cashier.entity.OrderPendingItemEntity;
import com.psi.cashier.service.OrderPendingItemService;
import com.psi.cashier.service.OrderPendingService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 挂单控制器
 * 提供挂单的REST API接口
 * 
 * @author PSI
 * @version 1.0.0
 */
@RestController
@RequestMapping("/psi/cashier/pending")
public class PendingController {

    private final OrderPendingService orderPendingService;
    private final OrderPendingItemService orderPendingItemService;

    public PendingController(OrderPendingService orderPendingService,
                            OrderPendingItemService orderPendingItemService) {
        this.orderPendingService = orderPendingService;
        this.orderPendingItemService = orderPendingItemService;
    }

    @GetMapping("/page")
    public PageResult<OrderPendingEntity> queryPage(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) Integer operatorId) {
        return orderPendingService.queryPage(pageNum, pageSize, operatorId);
    }

    @GetMapping("/all")
    public CommonResult<List<OrderPendingEntity>> getAllPending() {
        List<OrderPendingEntity> pendings = orderPendingService.getAllPending();
        return CommonResult.success(pendings);
    }

    @GetMapping("/{pendingNo}")
    public CommonResult<OrderPendingEntity> getByPendingNo(@PathVariable String pendingNo) {
        OrderPendingEntity pending = orderPendingService.getByPendingNo(pendingNo);
        if (pending == null) {
            return CommonResult.fail("挂单不存在");
        }
        return CommonResult.success(pending);
    }

    @GetMapping("/{pendingNo}/items")
    public CommonResult<List<OrderPendingItemEntity>> getPendingItems(@PathVariable String pendingNo) {
        List<OrderPendingItemEntity> items = orderPendingItemService.getByPendingNo(pendingNo);
        return CommonResult.success(items);
    }

    @GetMapping("/operator/{operatorId}")
    public CommonResult<List<OrderPendingEntity>> getByOperatorId(@PathVariable Integer operatorId) {
        List<OrderPendingEntity> pendings = orderPendingService.getByOperatorId(operatorId);
        return CommonResult.success(pendings);
    }

    @PostMapping
    public CommonResult<OrderPendingEntity> create(@RequestBody OrderPendingEntity pending) {
        OrderPendingEntity saved = orderPendingService.save(pending);
        return CommonResult.success(saved);
    }

    @PutMapping("/{pendingNo}")
    public CommonResult<OrderPendingEntity> update(@PathVariable String pendingNo, @RequestBody OrderPendingEntity pending) {
        OrderPendingEntity existing = orderPendingService.getByPendingNo(pendingNo);
        if (existing == null) {
            return CommonResult.fail("挂单不存在");
        }
        pending.setId(existing.getId());
        pending.setPendingNo(pendingNo);
        orderPendingService.update(pending);
        return CommonResult.success(pending);
    }

    @DeleteMapping("/{pendingNo}")
    public CommonResult<Void> delete(@PathVariable String pendingNo) {
        boolean deleted = orderPendingService.deleteByPendingNo(pendingNo);
        if (!deleted) {
            return CommonResult.fail("挂单不存在");
        }
        return CommonResult.success(null);
    }
}