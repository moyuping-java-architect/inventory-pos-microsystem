package com.psi.stock.controller;

import com.psi.stock.dto.StockBatchDTO;
import com.psi.stock.dto.StockBatchQueryDTO;
import com.psi.stock.service.StockBatchService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/psi/stock/batch")
public class StockBatchController {

    private final StockBatchService stockBatchService;

    public StockBatchController(StockBatchService stockBatchService) {
        this.stockBatchService = stockBatchService;
    }

    @GetMapping("/{id}")
    public CommonResult<StockBatchDTO> getById(@PathVariable Long id) {
        return stockBatchService.getById(id);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<StockBatchDTO>> list(@RequestBody StockBatchQueryDTO queryDTO) {
        return CommonResult.success(stockBatchService.list(queryDTO));
    }

    @PutMapping("/{id}/status/{status}")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return stockBatchService.updateStatus(id, status);
    }

    /**
     * 根据商品编码查询有效批次（按保质期升序，FIFO）
     */
    @GetMapping("/goods/{goodsCode}")
    public CommonResult<List<StockBatchDTO>> listValidByGoodsCode(@PathVariable String goodsCode) {
        StockBatchQueryDTO queryDTO = new StockBatchQueryDTO();
        queryDTO.setGoodsCode(goodsCode);
        queryDTO.setExpireDateStart(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(100);
        PageResult<StockBatchDTO> page = stockBatchService.list(queryDTO);
        List<StockBatchDTO> list = page.getList() != null ? page.getList() : Collections.emptyList();
        // 过滤已过期、无可用库存，并按过期时间升序
        list = list.stream()
                .filter(b -> b.getAvailableQuantity() != null && b.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0)
                .filter(b -> b.getExpireDate() == null || !LocalDate.parse(b.getExpireDate()).isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(StockBatchDTO::getExpireDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return CommonResult.success(list);
    }
}