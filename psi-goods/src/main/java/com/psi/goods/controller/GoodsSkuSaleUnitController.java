package com.psi.goods.controller;

import com.psi.goods.dto.GoodsSkuSaleUnitQueryDTO;
import com.psi.goods.entity.GoodsSkuSaleUnit;
import com.psi.goods.service.GoodsSkuSaleUnitService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SKU销售单位管理接口
 * 支持非洲场景下多种销售单位销售模式
 */
@RestController
@RequestMapping("/psi/goods/sku/sale-unit")
public class GoodsSkuSaleUnitController {

    private final GoodsSkuSaleUnitService goodsSkuSaleUnitService;

    public GoodsSkuSaleUnitController(GoodsSkuSaleUnitService goodsSkuSaleUnitService) {
        this.goodsSkuSaleUnitService = goodsSkuSaleUnitService;
    }

    /**
     * 根据SKU ID查询所有销售单位
     */
    @GetMapping("/sku/{skuId}")
    public CommonResult<List<GoodsSkuSaleUnit>> getBySkuId(@PathVariable Long skuId) {
        List<GoodsSkuSaleUnit> saleUnitList = goodsSkuSaleUnitService.getBySkuId(skuId);
        return CommonResult.success(saleUnitList);
    }

    /**
     * 根据SKU ID查询启用的销售单位
     */
    @GetMapping("/sku/{skuId}/enabled")
    public CommonResult<List<GoodsSkuSaleUnit>> getEnabledBySkuId(@PathVariable Long skuId) {
        List<GoodsSkuSaleUnit> saleUnitList = goodsSkuSaleUnitService.getEnabledBySkuId(skuId);
        return CommonResult.success(saleUnitList);
    }

    /**
     * 根据SKU ID查询默认销售单位
     */
    @GetMapping("/sku/{skuId}/default")
    public CommonResult<GoodsSkuSaleUnit> getDefaultBySkuId(@PathVariable Long skuId) {
        GoodsSkuSaleUnit saleUnit = goodsSkuSaleUnitService.getDefaultBySkuId(skuId);
        if (saleUnit == null) {
            return CommonResult.fail("未设置默认销售单位");
        }
        return CommonResult.success(saleUnit);
    }

    /**
     * 根据商品统一编码查询所有销售单位
     */
    @GetMapping("/unify-code/{goodsUnifyCode}")
    public CommonResult<List<GoodsSkuSaleUnit>> getByUnifyCode(@PathVariable String goodsUnifyCode) {
        List<GoodsSkuSaleUnit> saleUnitList = goodsSkuSaleUnitService.getByUnifyCode(goodsUnifyCode);
        return CommonResult.success(saleUnitList);
    }

    /**
     * 根据商品统一编码分组查询各销售单位的最高价格
     * 用于收银端展示，保证利润最大化
     */
    @GetMapping("/unify-code/{goodsUnifyCode}/max-price")
    public CommonResult<List<GoodsSkuSaleUnit>> getMaxPriceByUnifyCode(@PathVariable String goodsUnifyCode) {
        List<GoodsSkuSaleUnit> saleUnitList = goodsSkuSaleUnitService.getMaxPriceByUnifyCode(goodsUnifyCode);
        return CommonResult.success(saleUnitList);
    }

    /**
     * 分页查询SKU销售单位
     */
    @GetMapping("/page")
    public CommonResult<PageResult<GoodsSkuSaleUnit>> queryPage(GoodsSkuSaleUnitQueryDTO queryDTO) {
        PageResult<GoodsSkuSaleUnit> pageResult = goodsSkuSaleUnitService.queryPage(queryDTO);
        return CommonResult.success(pageResult);
    }

    /**
     * 保存销售单位
     */
    @PostMapping
    public CommonResult<GoodsSkuSaleUnit> save(@RequestBody GoodsSkuSaleUnit saleUnit) {
        goodsSkuSaleUnitService.save(saleUnit);
        return CommonResult.success(saleUnit);
    }

    /**
     * 更新销售单位
     */
    @PutMapping
    public CommonResult<GoodsSkuSaleUnit> update(@RequestBody GoodsSkuSaleUnit saleUnit) {
        goodsSkuSaleUnitService.updateById(saleUnit);
        return CommonResult.success(saleUnit);
    }

    /**
     * 批量保存SKU销售单位
     */
    @PostMapping("/batch/{skuId}")
    public CommonResult<List<GoodsSkuSaleUnit>> saveBatch(@PathVariable Long skuId, @RequestBody List<GoodsSkuSaleUnit> saleUnitList) {
        goodsSkuSaleUnitService.saveBatchBySkuId(skuId, saleUnitList);
        return CommonResult.success(saleUnitList);
    }

    /**
     * 删除销售单位
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        goodsSkuSaleUnitService.removeById(id);
        return CommonResult.success();
    }

    /**
     * 删除SKU所有销售单位
     */
    @DeleteMapping("/sku/{skuId}")
    public CommonResult<Void> deleteBySkuId(@PathVariable Long skuId) {
        goodsSkuSaleUnitService.deleteBySkuId(skuId);
        return CommonResult.success();
    }

    /**
     * 设置默认销售单位
     */
    @PutMapping("/sku/{skuId}/default/{saleUnitId}")
    public CommonResult<Void> setDefault(@PathVariable Long skuId, @PathVariable Long saleUnitId) {
        boolean success = goodsSkuSaleUnitService.setDefault(skuId, saleUnitId);
        if (success) {
            return CommonResult.success();
        }
        return CommonResult.fail("设置失败");
    }
}