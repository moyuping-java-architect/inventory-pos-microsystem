package com.trademaster.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.trademaster.common.PageResult;
import com.trademaster.common.Result;
import com.trademaster.entity.Goods;
import com.trademaster.service.GoodsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {
    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping("/page")
    public Result<PageResult<Goods>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<Goods> result = goodsService.findPage(page, size, keyword);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), (int) result.getSize(), (int) result.getCurrent()));
    }

    @GetMapping("/{id}")
    public Result<Goods> getById(@PathVariable Long id) {
        return Result.success(goodsService.findById(id));
    }

    @GetMapping("/scan/{barCode}")
    public Result<Goods> scan(@PathVariable String barCode) {
        Goods goods = goodsService.findByBarCode(barCode);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        return Result.success(goods);
    }

    @GetMapping("/search")
    public Result<Goods> search(@RequestParam String keyword) {
        Goods goods = goodsService.findByCode(keyword);
        if (goods == null) {
            goods = goodsService.findByBarCode(keyword);
        }
        if (goods == null) {
            return Result.error("商品不存在");
        }
        return Result.success(goods);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Goods goods) {
        goodsService.save(goods);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Goods goods) {
        goodsService.save(goods);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goodsService.delete(id);
        return Result.success();
    }

    @GetMapping("/low-stock")
    public Result<List<Goods>> lowStock() {
        return Result.success(goodsService.findLowStock());
    }
}
