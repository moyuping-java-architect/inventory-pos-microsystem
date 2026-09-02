package com.psi.purchase.controller;

import com.psi.purchase.dto.PurchaseOrderMainDTO;
import com.psi.purchase.dto.PurchaseOrderQueryDTO;
import com.psi.purchase.dto.PurchaseOrderSaveDTO;
import com.psi.purchase.service.PurchaseOrderMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/purchase/order")
public class PurchaseOrderController {

    private final PurchaseOrderMainService purchaseOrderMainService;

    public PurchaseOrderController(PurchaseOrderMainService purchaseOrderMainService) {
        this.purchaseOrderMainService = purchaseOrderMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<PurchaseOrderMainDTO> getById(@PathVariable Long id) {
        return purchaseOrderMainService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<PurchaseOrderMainDTO> list(PurchaseOrderQueryDTO queryDTO) {
        return purchaseOrderMainService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<PurchaseOrderMainDTO> save(@RequestBody PurchaseOrderSaveDTO saveDTO) {
        return purchaseOrderMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<PurchaseOrderMainDTO> update(@PathVariable Long id, @RequestBody PurchaseOrderSaveDTO saveDTO) {
        return purchaseOrderMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return purchaseOrderMainService.delete(id);
    }

    @PutMapping("/{id}/audit")
    public CommonResult<Void> audit(@PathVariable Long id, @RequestParam Integer auditStatus) {
        return purchaseOrderMainService.audit(id, auditStatus);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return purchaseOrderMainService.updateStatus(id, status);
    }
}