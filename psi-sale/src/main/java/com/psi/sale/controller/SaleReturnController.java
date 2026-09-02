package com.psi.sale.controller;

import com.psi.sale.dto.SaleReturnMainDTO;
import com.psi.sale.dto.SaleReturnQueryDTO;
import com.psi.sale.dto.SaleReturnSaveDTO;
import com.psi.sale.service.SaleReturnMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/sale/return")
public class SaleReturnController {

    private final SaleReturnMainService saleReturnMainService;

    public SaleReturnController(SaleReturnMainService saleReturnMainService) {
        this.saleReturnMainService = saleReturnMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<SaleReturnMainDTO> getById(@PathVariable Long id) {
        return saleReturnMainService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<SaleReturnMainDTO>> list(@RequestBody SaleReturnQueryDTO queryDTO) {
        return CommonResult.success(saleReturnMainService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<SaleReturnMainDTO> save(@RequestBody SaleReturnSaveDTO saveDTO) {
        return saleReturnMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<SaleReturnMainDTO> update(@PathVariable Long id, @RequestBody SaleReturnSaveDTO saveDTO) {
        return saleReturnMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return saleReturnMainService.delete(id);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return saleReturnMainService.updateStatus(id, status);
    }
}