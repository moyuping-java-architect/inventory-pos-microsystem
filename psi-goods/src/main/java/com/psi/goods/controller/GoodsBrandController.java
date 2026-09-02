package com.psi.goods.controller;

import com.psi.goods.dto.GoodsBrandQueryDTO;
import com.psi.goods.entity.GoodsBrand;
import com.psi.goods.service.GoodsBrandService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品品牌管理接口
 */
@RestController
@RequestMapping("/psi/goods/brand")
public class GoodsBrandController {

    private final GoodsBrandService goodsBrandService;

    public GoodsBrandController(GoodsBrandService goodsBrandService) {
        this.goodsBrandService = goodsBrandService;
    }

    /**
     * 分页查询品牌
     */
    @GetMapping("/page")
    public PageResult<GoodsBrand> queryPage(GoodsBrandQueryDTO queryDTO) {
        return goodsBrandService.queryPage(queryDTO);
    }

    /**
     * 查询所有启用的品牌
     */
    @GetMapping("/list")
    public CommonResult<List<GoodsBrand>> getAll() {
        List<GoodsBrand> brands = goodsBrandService.getAllEnabled();
        return CommonResult.success(brands);
    }

    /**
     * 根据品牌编码查询
     */
    @GetMapping("/code/{brandCode}")
    public CommonResult<GoodsBrand> getByCode(@PathVariable String brandCode) {
        GoodsBrand brand = goodsBrandService.getByCode(brandCode);
        if (brand == null) {
            return CommonResult.fail("品牌不存在");
        }
        return CommonResult.success(brand);
    }

    /**
     * 根据品牌名称查询
     */
    @GetMapping("/name/{brandName}")
    public CommonResult<List<GoodsBrand>> getByName(@PathVariable String brandName) {
        List<GoodsBrand> brands = goodsBrandService.getByName(brandName);
        return CommonResult.success(brands);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public CommonResult<GoodsBrand> getById(@PathVariable Long id) {
        GoodsBrand brand = goodsBrandService.getById(id);
        if (brand == null) {
            return CommonResult.fail("品牌不存在");
        }
        return CommonResult.success(brand);
    }

    /**
     * 新增品牌
     */
    @PostMapping
    public CommonResult<GoodsBrand> create(@RequestBody GoodsBrand brand) {
        goodsBrandService.save(brand);
        return CommonResult.success(brand);
    }

    /**
     * 更新品牌
     */
    @PutMapping("/{id}")
    public CommonResult<GoodsBrand> update(@PathVariable Long id, @RequestBody GoodsBrand brand) {
        brand.setId(id);
        goodsBrandService.updateById(brand);
        return CommonResult.success(brand);
    }

    /**
     * 删除品牌
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        goodsBrandService.removeById(id);
        return CommonResult.success(null);
    }
}