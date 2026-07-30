package com.psi.stock.controller;

import com.psi.stock.dto.StockTransferMainDTO;
import com.psi.stock.dto.StockTransferQueryDTO;
import com.psi.stock.dto.StockTransferSaveDTO;
import com.psi.stock.service.StockTransferMainService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/stock/transfer")
public class StockTransferController {

    private final StockTransferMainService stockTransferMainService;

    public StockTransferController(StockTransferMainService stockTransferMainService) {
        this.stockTransferMainService = stockTransferMainService;
    }

    @GetMapping("/{id}")
    public CommonResult<StockTransferMainDTO> getById(@PathVariable Long id) {
        return stockTransferMainService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<StockTransferMainDTO>> list(@RequestBody StockTransferQueryDTO queryDTO) {
        return CommonResult.success(stockTransferMainService.list(queryDTO));
    }

    @PostMapping
    public CommonResult<StockTransferMainDTO> save(@RequestBody StockTransferSaveDTO saveDTO) {
        return stockTransferMainService.save(saveDTO);
    }

    @PutMapping("/{id}")
    public CommonResult<StockTransferMainDTO> update(@PathVariable Long id, @RequestBody StockTransferSaveDTO saveDTO) {
        return stockTransferMainService.update(id, saveDTO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return stockTransferMainService.delete(id);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return stockTransferMainService.updateStatus(id, status);
    }
}