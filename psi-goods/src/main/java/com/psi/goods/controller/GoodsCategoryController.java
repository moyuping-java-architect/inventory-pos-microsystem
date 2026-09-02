package com.psi.goods.controller;

import com.psi.goods.dto.GoodsCategoryQueryDTO;
import com.psi.goods.entity.GoodsCategory;
import com.psi.goods.service.GoodsCategoryService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理接口
 */
@RestController
@RequestMapping("/psi/goods/category")
public class GoodsCategoryController {

    private final GoodsCategoryService goodsCategoryService;

    public GoodsCategoryController(GoodsCategoryService goodsCategoryService) {
        this.goodsCategoryService = goodsCategoryService;
    }

    /**
     * 分页查询分类
     */
    @GetMapping("/page")
    public PageResult<GoodsCategory> queryPage(GoodsCategoryQueryDTO queryDTO) {
        return goodsCategoryService.queryPage(queryDTO);
    }

    /**
     * 查询所有分类（树形结构）
     */
    @GetMapping("/tree")
    public CommonResult<List<GoodsCategory>> getCategoryTree() {
        List<GoodsCategory> tree = goodsCategoryService.getCategoryTree();
        return CommonResult.success(tree);
    }

    /**
     * 查询顶级分类
     */
    @GetMapping("/top")
    public CommonResult<List<GoodsCategory>> getTopLevel() {
        List<GoodsCategory> categories = goodsCategoryService.getTopLevel();
        return CommonResult.success(categories);
    }

    /**
     * 查询子分类
     */
    @GetMapping("/children/{parentId}")
    public CommonResult<List<GoodsCategory>> getChildren(@PathVariable Long parentId) {
        List<GoodsCategory> categories = goodsCategoryService.getByParentId(parentId);
        return CommonResult.success(categories);
    }

    /**
     * 查询所有启用的分类
     */
    @GetMapping("/list")
    public CommonResult<List<GoodsCategory>> getAll() {
        List<GoodsCategory> categories = goodsCategoryService.getAllEnabled();
        return CommonResult.success(categories);
    }

    /**
     * 根据分类编码查询
     */
    @GetMapping("/code/{categoryCode}")
    public CommonResult<GoodsCategory> getByCode(@PathVariable String categoryCode) {
        GoodsCategory category = goodsCategoryService.getByCode(categoryCode);
        if (category == null) {
            return CommonResult.fail("分类不存在");
        }
        return CommonResult.success(category);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public CommonResult<GoodsCategory> getById(@PathVariable Long id) {
        GoodsCategory category = goodsCategoryService.getById(id);
        if (category == null) {
            return CommonResult.fail("分类不存在");
        }
        return CommonResult.success(category);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public CommonResult<GoodsCategory> create(@RequestBody GoodsCategory category) {
        goodsCategoryService.save(category);
        return CommonResult.success(category);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public CommonResult<GoodsCategory> update(@PathVariable Long id, @RequestBody GoodsCategory category) {
        category.setId(id);
        goodsCategoryService.updateById(category);
        return CommonResult.success(category);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        goodsCategoryService.removeById(id);
        return CommonResult.success(null);
    }
}