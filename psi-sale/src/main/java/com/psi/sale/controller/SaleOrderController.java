package com.psi.sale.controller;

import com.psi.sale.dto.SaleOrderMainDTO;
import com.psi.sale.dto.SaleOrderQueryDTO;
import com.psi.sale.dto.SaleOrderSaveDTO;
import com.psi.sale.service.SaleOrderMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/sale/order")
public class SaleOrderController {

    private final SaleOrderMainService saleOrderMainService;

    public SaleOrderController(SaleOrderMainService saleOrderMainService) {
        this.saleOrderMainService = saleOrderMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<SaleOrderMainDTO> getById(@PathVariable Long id) {
        return saleOrderMainService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<SaleOrderMainDTO>> list(@RequestBody SaleOrderQueryDTO queryDTO) {
        return CommonResult.success(saleOrderMainService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<SaleOrderMainDTO> save(@RequestBody SaleOrderSaveDTO saveDTO) {
        return saleOrderMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<SaleOrderMainDTO> update(@PathVariable Long id, @RequestBody SaleOrderSaveDTO saveDTO) {
        return saleOrderMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return saleOrderMainService.delete(id);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return saleOrderMainService.updateStatus(id, status);
    }
}