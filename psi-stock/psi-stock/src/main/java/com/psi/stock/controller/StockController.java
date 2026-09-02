package com.psi.stock.controller;

import com.psi.stock.dto.StockBatchOperateDTO;
import com.psi.stock.dto.StockDTO;
import com.psi.stock.dto.StockOperateDTO;
import com.psi.stock.dto.StockQueryDTO;
import com.psi.stock.service.StockService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{id}")
    public CommonResult<StockDTO> getById(@PathVariable Long id) {
        return stockService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<StockDTO>> list(@RequestBody StockQueryDTO queryDTO) {
        return CommonResult.success(stockService.list(queryDTO));
    }

    @GetMapping("/query")
    public CommonResult<StockDTO> getStock(@RequestParam String warehouseCode, @RequestParam String skuCode) {
        return stockService.getStock(warehouseCode, skuCode);
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return stockService.updateStatus(id, status);
    }

    @PostMapping("/increase")
    public CommonResult<Void> increase(@RequestBody StockOperateDTO dto) {
        return stockService.increaseStock(dto.getWarehouseCode(), dto.getGoodsCode(), dto.getSkuCode(),
                dto.getQuantity(), dto.getCostPrice(), dto.getSourceNo(), dto.getSourceType());
    }

    @PostMapping("/decrease")
    public CommonResult<Void> decrease(@RequestBody StockOperateDTO dto) {
        return stockService.decreaseStock(dto.getWarehouseCode(), dto.getSkuCode(),
                dto.getQuantity(), dto.getSourceNo(), dto.getSourceType());
    }

    @PostMapping("/lock")
    public CommonResult<Void> lock(@RequestBody StockOperateDTO dto) {
        return stockService.lockStock(dto.getWarehouseCode(), dto.getSkuCode(),
                dto.getQuantity(), dto.getSourceNo(), dto.getSourceType());
    }

    @PostMapping("/release")
    public CommonResult<Void> release(@RequestBody StockOperateDTO dto) {
        return stockService.releaseStock(dto.getWarehouseCode(), dto.getSkuCode(),
                dto.getQuantity(), dto.getSourceNo(), dto.getSourceType());
    }

    @PostMapping("/confirm")
    public CommonResult<Void> confirm(@RequestBody StockOperateDTO dto) {
        return stockService.confirmStock(dto.getWarehouseCode(), dto.getSkuCode(),
                dto.getQuantity(), dto.getSourceNo(), dto.getSourceType());
    }

    @PostMapping("/batch/decrease")
    public CommonResult<Void> batchDecrease(@RequestBody StockBatchOperateDTO dto) {
        return stockService.batchDecreaseStock(dto.getItems(), dto.getSourceNo(), dto.getSourceType());
    }
}
