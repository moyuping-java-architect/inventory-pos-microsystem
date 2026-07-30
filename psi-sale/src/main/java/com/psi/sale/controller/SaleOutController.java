package com.psi.sale.controller;

import com.psi.sale.dto.SaleOutMainDTO;
import com.psi.sale.dto.SaleOutQueryDTO;
import com.psi.sale.dto.SaleOutSaveDTO;
import com.psi.sale.service.SaleOutMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/sale/out")
public class SaleOutController {

    private final SaleOutMainService saleOutMainService;

    public SaleOutController(SaleOutMainService saleOutMainService) {
        this.saleOutMainService = saleOutMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<SaleOutMainDTO> getById(@PathVariable Long id) {
        return saleOutMainService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<SaleOutMainDTO>> list(@RequestBody SaleOutQueryDTO queryDTO) {
        return CommonResult.success(saleOutMainService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<SaleOutMainDTO> save(@RequestBody SaleOutSaveDTO saveDTO) {
        return saleOutMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<SaleOutMainDTO> update(@PathVariable Long id, @RequestBody SaleOutSaveDTO saveDTO) {
        return saleOutMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return saleOutMainService.delete(id);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return saleOutMainService.updateStatus(id, status);
    }
}