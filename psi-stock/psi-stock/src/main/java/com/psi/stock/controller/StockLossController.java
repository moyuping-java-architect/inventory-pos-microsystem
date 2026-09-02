package com.psi.stock.controller;

import com.psi.stock.dto.StockLossMainDTO;
import com.psi.stock.dto.StockLossQueryDTO;
import com.psi.stock.dto.StockLossSaveDTO;
import com.psi.stock.service.StockLossMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/stock/loss")
public class StockLossController {

    private final StockLossMainService stockLossMainService;

    public StockLossController(StockLossMainService stockLossMainService) {
        this.stockLossMainService = stockLossMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<StockLossMainDTO> getById(@PathVariable Long id) {
        return stockLossMainService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<StockLossMainDTO>> list(@RequestBody StockLossQueryDTO queryDTO) {
        return CommonResult.success(stockLossMainService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<StockLossMainDTO> save(@RequestBody StockLossSaveDTO saveDTO) {
        return stockLossMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<StockLossMainDTO> update(@PathVariable Long id, @RequestBody StockLossSaveDTO saveDTO) {
        return stockLossMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return stockLossMainService.delete(id);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return stockLossMainService.updateStatus(id, status);
    }
}