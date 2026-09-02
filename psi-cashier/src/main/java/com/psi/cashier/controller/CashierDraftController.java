package com.psi.cashier.controller;

import com.psi.cashier.dto.PendingMainSaveDTO;
import com.psi.cashier.entity.OrderPendingEntity;
import com.psi.cashier.service.OrderPendingService;
import com.psi.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/psi/cashier/draft")
@RequiredArgsConstructor
public class CashierDraftController {

    private final OrderPendingService orderPendingService;

    @PostMapping("/save")
    public CommonResult<OrderPendingEntity> saveDraft(@RequestBody PendingMainSaveDTO dto) {
        OrderPendingEntity draft = orderPendingService.savePending(dto);
        return CommonResult.success(draft);
    }

    @GetMapping("/{pendingNo}")
    public CommonResult<PendingMainSaveDTO> getDraft(@PathVariable String pendingNo) {
        PendingMainSaveDTO draft = orderPendingService.getPendingByNo(pendingNo);
        if (draft == null) {
            return CommonResult.fail("挂单不存在");
        }
        return CommonResult.success(draft);
    }

    @GetMapping("/list/operator/{operatorId}")
    public CommonResult<List<PendingMainSaveDTO>> listByOperatorId(@PathVariable Integer operatorId) {
        List<PendingMainSaveDTO> drafts = orderPendingService.listByOperatorId(operatorId);
        return CommonResult.success(drafts);
    }

    @GetMapping("/list/shop/{shopCode}")
    public CommonResult<List<PendingMainSaveDTO>> listByShopCode(@PathVariable String shopCode) {
        List<PendingMainSaveDTO> drafts = orderPendingService.listByShopCode(shopCode);
        return CommonResult.success(drafts);
    }

    @DeleteMapping("/{pendingNo}")
    public CommonResult<Boolean> deleteDraft(@PathVariable String pendingNo) {
        boolean success = orderPendingService.deleteByPendingNo(pendingNo);
        if (!success) {
            return CommonResult.fail("挂单不存在");
        }
        return CommonResult.success(true);
    }
}