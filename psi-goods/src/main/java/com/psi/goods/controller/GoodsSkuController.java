package com.psi.goods.controller;

import com.psi.goods.dto.GoodsSkuQueryDTO;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.service.GoodsSkuService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品SKU管理接口
 */
@RestController
@RequestMapping("/psi/goods/sku")
public class GoodsSkuController {

    private final GoodsSkuService goodsSkuService;

    public GoodsSkuController(GoodsSkuService goodsSkuService) {
        this.goodsSkuService = goodsSkuService;
    }

    /**
     * 分页查询SKU
     */
    @GetMapping("/page")
    public PageResult<GoodsSku> queryPage(GoodsSkuQueryDTO queryDTO) {
        return goodsSkuService.queryPage(queryDTO);
    }

    /**
     * 根据统一编码查询所有SKU
     */
    @GetMapping("/unify-code/{goodsUnifyCode}")
    public CommonResult<List<GoodsSku>> getByUnifyCode(@PathVariable String goodsUnifyCode) {
        List<GoodsSku> skuList = goodsSkuService.getByUnifyCode(goodsUnifyCode);
        return CommonResult.success(skuList);
    }

    /**
     * 根据商品ID查询SKU列表
     */
    @GetMapping("/goods/{goodsId}")
    public CommonResult<List<GoodsSku>> getByGoodsId(@PathVariable Long goodsId) {
        GoodsSkuQueryDTO queryDTO = new GoodsSkuQueryDTO();
        queryDTO.setGoodsId(goodsId);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(Integer.MAX_VALUE);
        PageResult<GoodsSku> page = goodsSkuService.queryPage(queryDTO);
        return CommonResult.success(page.getList());
    }

    /**
     * 获取最高价格的SKU
     */
    @GetMapping("/highest-price/{goodsUnifyCode}")
    public CommonResult<GoodsSku> getHighestPriceSku(@PathVariable String goodsUnifyCode) {
        GoodsSku sku = goodsSkuService.getHighestPriceSku(goodsUnifyCode);
        if (sku == null) {
            return CommonResult.fail("未找到SKU");
        }
        return CommonResult.success(sku);
    }

    /**
     * 根据ID查询SKU
     */
    @GetMapping("/{id}")
    public CommonResult<GoodsSku> getById(@PathVariable Long id) {
        GoodsSku sku = goodsSkuService.getById(id);
        if (sku == null) {
            return CommonResult.fail("SKU不存在");
        }
        return CommonResult.success(sku);
    }

    /**
     * 新增SKU
     */
    @PostMapping
    public CommonResult<GoodsSku> create(@RequestBody GoodsSku sku) {
        goodsSkuService.save(sku);
        return CommonResult.success(sku);
    }

    /**
     * 更新SKU
     */
    @PutMapping("/{id}")
    public CommonResult<GoodsSku> update(@PathVariable Long id, @RequestBody GoodsSku sku) {
        sku.setId(id);
        goodsSkuService.updateById(sku);
        return CommonResult.success(sku);
    }

    /**
     * 删除SKU
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        goodsSkuService.removeById(id);
        return CommonResult.success(null);
    }
}