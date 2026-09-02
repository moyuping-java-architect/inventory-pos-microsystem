package com.psi.purchase.controller;

import com.psi.purchase.dto.PurchaseReturnMainDTO;
import com.psi.purchase.dto.PurchaseReturnQueryDTO;
import com.psi.purchase.dto.PurchaseReturnSaveDTO;
import com.psi.purchase.service.PurchaseReturnMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/purchase/return")
public class PurchaseReturnController {

    private final PurchaseReturnMainService purchaseReturnMainService;

    public PurchaseReturnController(PurchaseReturnMainService purchaseReturnMainService) {
        this.purchaseReturnMainService = purchaseReturnMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<PurchaseReturnMainDTO> getById(@PathVariable Long id) {
        return purchaseReturnMainService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<PurchaseReturnMainDTO> list(PurchaseReturnQueryDTO queryDTO) {
        return purchaseReturnMainService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<PurchaseReturnMainDTO> save(@RequestBody PurchaseReturnSaveDTO saveDTO) {
        return purchaseReturnMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<PurchaseReturnMainDTO> update(@PathVariable Long id, @RequestBody PurchaseReturnSaveDTO saveDTO) {
        return purchaseReturnMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return purchaseReturnMainService.delete(id);
    }

    @PutMapping("/{id}/audit")
    public CommonResult<Void> audit(@PathVariable Long id, @RequestParam Integer auditStatus) {
        return purchaseReturnMainService.audit(id, auditStatus);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return purchaseReturnMainService.updateStatus(id, status);
    }
}