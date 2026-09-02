package com.psi.cashier.controller;

import com.psi.cashier.entity.ProductSkuSaleUnit;
import com.psi.cashier.service.ProductSkuSaleUnitService;
import com.psi.cashier.service.SettlementCheckService;
import com.psi.common.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SKU销售单位管理接口
 */
@RestController
@RequestMapping("/psi/cashier/product-sku-sale-unit")
public class ProductSkuSaleUnitController {

    private final ProductSkuSaleUnitService productSkuSaleUnitService;
    private final SettlementCheckService settlementCheckService;

    public ProductSkuSaleUnitController(ProductSkuSaleUnitService productSkuSaleUnitService,
                                       SettlementCheckService settlementCheckService) {
        this.productSkuSaleUnitService = productSkuSaleUnitService;
        this.settlementCheckService = settlementCheckService;
    }

    @GetMapping("/barcode/{barcode}")
    public CommonResult<List<ProductSkuSaleUnit>> getByBarcode(@PathVariable String barcode) {
        // 检查日结状态
        String unsettledDate = settlementCheckService.getUnsettledDateStr();
        if (unsettledDate != null) {
            return CommonResult.fail("请先完成 " + unsettledDate + " 的日结，然后才能进行销售");
        }
        
        List<ProductSkuSaleUnit> list = productSkuSaleUnitService.getByBarcode(barcode);
        if (list == null || list.isEmpty()) {
            return CommonResult.fail("商品不存在");
        }
        return CommonResult.success(list);
    }

    @GetMapping("/sku-no/{skuNo}")
    public CommonResult<List<ProductSkuSaleUnit>> getBySkuNo(@PathVariable String skuNo) {
        List<ProductSkuSaleUnit> list = productSkuSaleUnitService.getBySkuNo(skuNo);
        if (list == null || list.isEmpty()) {
            return CommonResult.fail("商品不存在");
        }
        return CommonResult.success(list);
    }

    @GetMapping("/sku/{skuId}")
    public CommonResult<List<ProductSkuSaleUnit>> getBySkuId(@PathVariable Long skuId) {
        List<ProductSkuSaleUnit> list = productSkuSaleUnitService.getBySkuId(skuId);
        return CommonResult.success(list);
    }

    @GetMapping("/tenant/{tenantId}")
    public CommonResult<List<ProductSkuSaleUnit>> getByTenantId(@PathVariable Long tenantId) {
        List<ProductSkuSaleUnit> list = productSkuSaleUnitService.getByTenantId(tenantId);
        return CommonResult.success(list);
    }

    @GetMapping("/search")
    public CommonResult<List<ProductSkuSaleUnit>> searchByGoodsName(@RequestParam String goodsName) {
        List<ProductSkuSaleUnit> list = productSkuSaleUnitService.searchByGoodsName(goodsName);
        return CommonResult.success(list);
    }

    @GetMapping("/{id}")
    public CommonResult<ProductSkuSaleUnit> getById(@PathVariable Long id) {
        ProductSkuSaleUnit entity = productSkuSaleUnitService.getById(id);
        if (entity == null) {
            return CommonResult.fail("记录不存在");
        }
        return CommonResult.success(entity);
    }

    @PostMapping
    public CommonResult<ProductSkuSaleUnit> save(@RequestBody ProductSkuSaleUnit entity) {
        productSkuSaleUnitService.save(entity);
        return CommonResult.success(entity);
    }

    @PutMapping
    public CommonResult<ProductSkuSaleUnit> update(@RequestBody ProductSkuSaleUnit entity) {
        productSkuSaleUnitService.updateById(entity);
        return CommonResult.success(entity);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        productSkuSaleUnitService.removeById(id);
        return CommonResult.success();
    }
}