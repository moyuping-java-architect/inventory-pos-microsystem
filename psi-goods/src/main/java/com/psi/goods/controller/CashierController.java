package com.psi.goods.controller;

import com.psi.goods.dto.CashierSaleUnitDTO;
import com.psi.goods.dto.CashierGoodsQueryDTO;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.entity.GoodsSkuSaleUnit;
import com.psi.goods.service.GoodsSkuService;
import com.psi.goods.service.GoodsSkuSaleUnitService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收银端商品查询接口
 * 前端收银查找商品优先查询SKU销售单位表
 */
@RestController
@RequestMapping("/psi/goods/cashier")
public class CashierController {

    private final GoodsSkuService goodsSkuService;
    private final GoodsSkuSaleUnitService goodsSkuSaleUnitService;

    public CashierController(GoodsSkuService goodsSkuService, GoodsSkuSaleUnitService goodsSkuSaleUnitService) {
        this.goodsSkuService = goodsSkuService;
        this.goodsSkuSaleUnitService = goodsSkuSaleUnitService;
    }

    /**
     * 收银端查询商品销售单位（按goods_unify_code和销售单位分组）
     * 前端收银查找商品时使用此接口
     * 同编码下同销售单位取最高价格展示，保证利润最大化
     */
    @GetMapping("/sale-units")
    public PageResult<CashierSaleUnitDTO> querySaleUnits(CashierGoodsQueryDTO queryDTO) {
        return goodsSkuSaleUnitService.queryForCashier(queryDTO);
    }

    /**
     * 根据统一编码查询销售单位列表（收银端展示）
     */
    @GetMapping("/sale-units/{goodsUnifyCode}")
    public CommonResult<List<CashierSaleUnitDTO>> getSaleUnitsByUnifyCode(@PathVariable String goodsUnifyCode) {
        List<CashierSaleUnitDTO> saleUnitList = goodsSkuSaleUnitService.getCashierSaleUnits(goodsUnifyCode);
        return CommonResult.success(saleUnitList);
    }

    /**
     * 根据统一编码查询各销售单位最高价格列表
     */
    @GetMapping("/sale-units/{goodsUnifyCode}/max-price")
    public CommonResult<List<GoodsSkuSaleUnit>> getMaxPriceSaleUnits(@PathVariable String goodsUnifyCode) {
        List<GoodsSkuSaleUnit> saleUnitList = goodsSkuSaleUnitService.getMaxPriceByUnifyCode(goodsUnifyCode);
        return CommonResult.success(saleUnitList);
    }

    /**
     * 收银端查询商品（按goods_unify_code和销售单位分组）
     * 参照收银微服务查询SKU销售单位，返回各销售单位明细
     * 同编码下同销售单位取最高价格展示，保证利润最大化
     */
    @GetMapping("/goods")
    public PageResult<CashierSaleUnitDTO> queryGoods(CashierGoodsQueryDTO queryDTO) {
        return goodsSkuSaleUnitService.queryForCashier(queryDTO);
    }

    /**
     * 根据统一编码查询所有SKU
     */
    @GetMapping("/sku/{goodsUnifyCode}")
    public CommonResult<List<GoodsSku>> getSkuByUnifyCode(@PathVariable String goodsUnifyCode) {
        List<GoodsSku> skuList = goodsSkuService.getByUnifyCode(goodsUnifyCode);
        return CommonResult.success(skuList);
    }

    /**
     * 根据统一编码获取最高价格的SKU
     */
    @GetMapping("/sku/highest-price/{goodsUnifyCode}")
    public CommonResult<GoodsSku> getHighestPriceSku(@PathVariable String goodsUnifyCode) {
        GoodsSku sku = goodsSkuService.getHighestPriceSku(goodsUnifyCode);
        if (sku == null) {
            return CommonResult.fail("商品不存在");
        }
        return CommonResult.success(sku);
    }

    /**
     * 根据条码查询商品（按最高价格展示）
     * 确保扫码收银时使用最高价格，保证利润最大化
     * 优先查询SKU销售单位表，支持多种销售单位
     */
    @GetMapping("/goods/barcode/{barcode}")
    public CommonResult<List<CashierSaleUnitDTO>> getGoodsByBarcode(@PathVariable String barcode) {
        List<CashierSaleUnitDTO> saleUnits = goodsSkuSaleUnitService.getByBarcode(barcode);
        if (saleUnits == null || saleUnits.isEmpty()) {
            return CommonResult.fail("条码不存在");
        }
        return CommonResult.success(saleUnits);
    }
}