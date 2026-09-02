package com.psi.stock.controller;

import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.stock.dto.StockWarnDTO;
import com.psi.stock.dto.StockWarnQueryDTO;
import com.psi.stock.service.StockWarnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock/warn")
@Tag(name = "库存预警", description = "库存预警管理")
public class StockWarnController {

    private final StockWarnService stockWarnService;

    public StockWarnController(StockWarnService stockWarnService) {
        this.stockWarnService = stockWarnService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询预警")
    public CommonResult<PageResult<StockWarnDTO>> page(StockWarnQueryDTO queryDTO) {
        return CommonResult.success(stockWarnService.page(queryDTO));
    }

    @PostMapping
    @Operation(summary = "新增预警设置")
    public CommonResult<Void> add(@RequestBody StockWarnDTO dto) {
        return stockWarnService.add(dto);
    }

    @PutMapping
    @Operation(summary = "修改预警设置")
    public CommonResult<Void> update(@RequestBody StockWarnDTO dto) {
        return stockWarnService.update(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预警")
    public CommonResult<Void> delete(@PathVariable Long id) {
        return stockWarnService.delete(id);
    }

    @GetMapping("/low")
    @Operation(summary = "获取库存不足预警列表")
    public CommonResult<List<StockWarnDTO>> getLowStockList(
            @RequestParam(required = false) String warehouseCode) {
        return stockWarnService.getLowStockList(warehouseCode);
    }

    @GetMapping("/high")
    @Operation(summary = "获取库存超储预警列表")
    public CommonResult<List<StockWarnDTO>> getHighStockList(
            @RequestParam(required = false) String warehouseCode) {
        return stockWarnService.getHighStockList(warehouseCode);
    }

    @GetMapping("/low/count")
    @Operation(summary = "获取库存不足预警数量")
    public CommonResult<Integer> getLowStockCount(
            @RequestParam(required = false) String warehouseCode) {
        return stockWarnService.getLowStockCount(warehouseCode);
    }

    @GetMapping("/high/count")
    @Operation(summary = "获取库存超储预警数量")
    public CommonResult<Integer> getHighStockCount(
            @RequestParam(required = false) String warehouseCode) {
        return stockWarnService.getHighStockCount(warehouseCode);
    }
}
