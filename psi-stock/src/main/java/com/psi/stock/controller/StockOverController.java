package com.psi.stock.controller;

import com.psi.stock.dto.StockOverMainDTO;
import com.psi.stock.dto.StockOverQueryDTO;
import com.psi.stock.dto.StockOverSaveDTO;
import com.psi.stock.service.StockOverMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/stock/over")
public class StockOverController {

    private final StockOverMainService stockOverMainService;

    public StockOverController(StockOverMainService stockOverMainService) {
        this.stockOverMainService = stockOverMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<StockOverMainDTO> getById(@PathVariable Long id) {
        return stockOverMainService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<StockOverMainDTO>> list(@RequestBody StockOverQueryDTO queryDTO) {
        return CommonResult.success(stockOverMainService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<StockOverMainDTO> save(@RequestBody StockOverSaveDTO saveDTO) {
        return stockOverMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<StockOverMainDTO> update(@PathVariable Long id, @RequestBody StockOverSaveDTO saveDTO) {
        return stockOverMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return stockOverMainService.delete(id);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return stockOverMainService.updateStatus(id, status);
    }
}