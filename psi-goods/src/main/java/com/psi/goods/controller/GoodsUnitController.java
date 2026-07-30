package com.psi.goods.controller;

import com.psi.goods.dto.GoodsUnitQueryDTO;
import com.psi.goods.entity.GoodsUnit;
import com.psi.goods.service.GoodsUnitService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品单位管理接口
 */
@RestController
@RequestMapping("/psi/goods/unit")
public class GoodsUnitController {

    private final GoodsUnitService goodsUnitService;

    public GoodsUnitController(GoodsUnitService goodsUnitService) {
        this.goodsUnitService = goodsUnitService;
    }

    /**
     * 分页查询单位
     */
    @GetMapping("/page")
    public PageResult<GoodsUnit> queryPage(GoodsUnitQueryDTO queryDTO) {
        return goodsUnitService.queryPage(queryDTO);
    }

    /**
     * 查询所有启用的单位
     */
    @GetMapping("/list")
    public CommonResult<List<GoodsUnit>> getAllUnits() {
        List<GoodsUnit> units = goodsUnitService.getAllEnabled();
        return CommonResult.success(units);
    }

    /**
     * 根据单位类型查询
     * @param unitType WEIGHT-重量单位，VOLUME-体积单位，COUNT-计数单位
     */
    @GetMapping("/type/{unitType}")
    public CommonResult<List<GoodsUnit>> getByUnitType(@PathVariable String unitType) {
        List<GoodsUnit> units = goodsUnitService.getByUnitType(unitType);
        return CommonResult.success(units);
    }

    /**
     * 根据单位符号查询
     */
    @GetMapping("/symbol/{unitSymbol}")
    public CommonResult<GoodsUnit> getBySymbol(@PathVariable String unitSymbol) {
        GoodsUnit unit = goodsUnitService.getBySymbol(unitSymbol);
        if (unit == null) {
            return CommonResult.fail("单位不存在");
        }
        return CommonResult.success(unit);
    }

    /**
     * 根据ID查询单位
     */
    @GetMapping("/{id}")
    public CommonResult<GoodsUnit> getById(@PathVariable Long id) {
        GoodsUnit unit = goodsUnitService.getById(id);
        if (unit == null) {
            return CommonResult.fail("单位不存在");
        }
        return CommonResult.success(unit);
    }

    /**
     * 新增单位
     */
    @PostMapping
    public CommonResult<GoodsUnit> create(@RequestBody GoodsUnit unit) {
        goodsUnitService.save(unit);
        return CommonResult.success(unit);
    }

    /**
     * 更新单位
     */
    @PutMapping("/{id}")
    public CommonResult<GoodsUnit> update(@PathVariable Long id, @RequestBody GoodsUnit unit) {
        unit.setId(id);
        goodsUnitService.updateById(unit);
        return CommonResult.success(unit);
    }

    /**
     * 删除单位
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        goodsUnitService.removeById(id);
        return CommonResult.success(null);
    }
}