package com.psi.stock.controller;

import com.psi.stock.dto.StockCheckMainDTO;
import com.psi.stock.dto.StockCheckQueryDTO;
import com.psi.stock.dto.StockCheckSaveDTO;
import com.psi.stock.service.StockCheckMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/stock/check")
public class StockCheckController {

    private final StockCheckMainService stockCheckMainService;

    public StockCheckController(StockCheckMainService stockCheckMainService) {
        this.stockCheckMainService = stockCheckMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<StockCheckMainDTO> getById(@PathVariable Long id) {
        return stockCheckMainService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<StockCheckMainDTO>> list(@RequestBody StockCheckQueryDTO queryDTO) {
        return CommonResult.success(stockCheckMainService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<StockCheckMainDTO> save(@RequestBody StockCheckSaveDTO saveDTO) {
        return stockCheckMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<StockCheckMainDTO> update(@PathVariable Long id, @RequestBody StockCheckSaveDTO saveDTO) {
        return stockCheckMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return stockCheckMainService.delete(id);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return stockCheckMainService.updateStatus(id, status);
    }
}