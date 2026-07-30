package com.psi.stock.controller;

import com.psi.stock.dto.StockFlowDTO;
import com.psi.stock.dto.StockFlowQueryDTO;
import com.psi.stock.service.StockFlowService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/stock/flow")
public class StockFlowController {

    private final StockFlowService stockFlowService;

    public StockFlowController(StockFlowService stockFlowService) {
        this.stockFlowService = stockFlowService;
    }

    @GetMapping("/{id}")
    public CommonResult<StockFlowDTO> getById(@PathVariable Long id) {
        return stockFlowService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<StockFlowDTO>> list(@RequestBody StockFlowQueryDTO queryDTO) {
        return CommonResult.success(stockFlowService.list(queryDTO));
    }
}