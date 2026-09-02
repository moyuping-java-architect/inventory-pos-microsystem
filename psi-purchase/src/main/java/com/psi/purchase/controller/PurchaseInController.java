package com.psi.purchase.controller;

import com.psi.purchase.dto.PurchaseInMainDTO;
import com.psi.purchase.dto.PurchaseInQueryDTO;
import com.psi.purchase.dto.PurchaseInSaveDTO;
import com.psi.purchase.service.PurchaseInMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/purchase/in")
public class PurchaseInController {

    private final PurchaseInMainService purchaseInMainService;

    public PurchaseInController(PurchaseInMainService purchaseInMainService) {
        this.purchaseInMainService = purchaseInMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<PurchaseInMainDTO> getById(@PathVariable Long id) {
        return purchaseInMainService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<PurchaseInMainDTO> list(PurchaseInQueryDTO queryDTO) {
        return purchaseInMainService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<PurchaseInMainDTO> save(@RequestBody PurchaseInSaveDTO saveDTO) {
        return purchaseInMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<PurchaseInMainDTO> update(@PathVariable Long id, @RequestBody PurchaseInSaveDTO saveDTO) {
        return purchaseInMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return purchaseInMainService.delete(id);
    }

    @PutMapping("/{id}/audit")
    public CommonResult<Void> audit(@PathVariable Long id, @RequestParam Integer auditStatus) {
        return purchaseInMainService.audit(id, auditStatus);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return purchaseInMainService.updateStatus(id, status);
    }
}