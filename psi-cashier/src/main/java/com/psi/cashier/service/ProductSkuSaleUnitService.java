package com.psi.cashier.service;

import com.psi.cashier.entity.ProductSkuSaleUnit;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * SKU销售单位服务接口
 */
public interface ProductSkuSaleUnitService extends IService<ProductSkuSaleUnit> {

    List<ProductSkuSaleUnit> getByBarcode(String barcode);

    List<ProductSkuSaleUnit> getBySkuNo(String skuNo);

    List<ProductSkuSaleUnit> getBySkuId(Long skuId);

    List<ProductSkuSaleUnit> getByTenantId(Long tenantId);

    List<ProductSkuSaleUnit> searchByGoodsName(String goodsName);
}