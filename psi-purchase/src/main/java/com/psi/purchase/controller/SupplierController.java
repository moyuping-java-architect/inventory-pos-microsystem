package com.psi.purchase.controller;

import com.psi.purchase.dto.SupplierDTO;
import com.psi.purchase.dto.SupplierQueryDTO;
import com.psi.purchase.dto.SupplierSaveDTO;
import com.psi.purchase.service.SupplierService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/purchase/supplier")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/{id}")
    public CommonResult<SupplierDTO> getById(@PathVariable Long id) {
        return supplierService.getById(id);
    }

    @GetMapping("/list")
    public PageResult<SupplierDTO> list(SupplierQueryDTO queryDTO) {
        return supplierService.list(queryDTO);
    }

    @PostMapping
    public CommonResult<SupplierDTO> save(@RequestBody SupplierSaveDTO saveDTO) {
        return supplierService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<SupplierDTO> update(@PathVariable Long id, @RequestBody SupplierSaveDTO saveDTO) {
        return supplierService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return supplierService.delete(id);
    }

    @PutMapping("/{id}/status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return supplierService.updateStatus(id, status);
    }
}