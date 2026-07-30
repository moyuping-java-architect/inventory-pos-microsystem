package com.psi.goods.controller;

import com.psi.goods.dto.GoodsQueryDTO;
import com.psi.goods.entity.Goods;
import com.psi.goods.mq.producer.GoodsSyncProducer;
import com.psi.goods.service.GoodsService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理接口
 */
@RestController
@RequestMapping("/psi/goods")
public class GoodsController {

    private final GoodsService goodsService;
    private final GoodsSyncProducer goodsSyncProducer;

    public GoodsController(GoodsService goodsService, GoodsSyncProducer goodsSyncProducer) {
        this.goodsService = goodsService;
        this.goodsSyncProducer = goodsSyncProducer;
    }

    /**
     * 分页查询商品
     */
    @GetMapping("/page")
    public PageResult<Goods> queryPage(GoodsQueryDTO queryDTO) {
        return goodsService.queryPage(queryDTO);
    }

    /**
     * 查询所有启用的商品
     */
    @GetMapping("/list")
    public CommonResult<List<Goods>> getAll() {
        List<Goods> goods = goodsService.getAllEnabled();
        return CommonResult.success(goods);
    }

    /**
     * 根据商品编码查询
     */
    @GetMapping("/code/{goodsCode}")
    public CommonResult<Goods> getByCode(@PathVariable String goodsCode) {
        Goods goods = goodsService.getByCode(goodsCode);
        if (goods == null) {
            return CommonResult.fail("商品不存在");
        }
        return CommonResult.success(goods);
    }

    /**
     * 根据分类ID查询商品
     */
    @GetMapping("/by-category/{categoryId}")
    public CommonResult<List<Goods>> getByCategory(@PathVariable Long categoryId) {
        List<Goods> goods = goodsService.getByCategoryId(categoryId);
        return CommonResult.success(goods);
    }

    /**
     * 根据品牌ID查询商品
     */
    @GetMapping("/by-brand/{brandId}")
    public CommonResult<List<Goods>> getByBrand(@PathVariable Long brandId) {
        List<Goods> goods = goodsService.getByBrandId(brandId);
        return CommonResult.success(goods);
    }

    /**
     * 根据商品名称模糊查询
     */
    @GetMapping("/search/{goodsName}")
    public CommonResult<List<Goods>> searchByName(@PathVariable String goodsName) {
        List<Goods> goods = goodsService.getByName(goodsName);
        return CommonResult.success(goods);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public CommonResult<Goods> getById(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return CommonResult.fail("商品不存在");
        }
        return CommonResult.success(goods);
    }

    /**
     * 新增商品
     */
    @PostMapping
    public CommonResult<Goods> create(@RequestBody Goods goods) {
        goodsService.save(goods);
        return CommonResult.success(goods);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public CommonResult<Goods> update(@PathVariable Long id, @RequestBody Goods goods) {
        goods.setId(id);
        goodsService.updateById(goods);
        return CommonResult.success(goods);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        goodsService.removeById(id);
        return CommonResult.success(null);
    }

    /**
     * 手动触发商品数据上传
     * 将商品数据同步到中间微服务（psi-sync）
     */
    @PostMapping("/upload")
    public CommonResult<String> uploadData() {
        goodsSyncProducer.syncAllAsync();
        return CommonResult.success("商品数据上传任务已触发，请稍后查看日志确认上传结果");
    }
}