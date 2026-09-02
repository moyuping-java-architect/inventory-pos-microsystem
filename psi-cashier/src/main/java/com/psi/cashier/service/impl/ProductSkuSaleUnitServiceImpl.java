package com.psi.cashier.service.impl;

import com.psi.cashier.entity.ProductSkuSaleUnit;
import com.psi.cashier.mapper.ProductSkuSaleUnitMapper;
import com.psi.cashier.service.ProductSkuSaleUnitService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SKU销售单位服务实现
 */
@Service
public class ProductSkuSaleUnitServiceImpl extends ServiceImpl<ProductSkuSaleUnitMapper, ProductSkuSaleUnit> implements ProductSkuSaleUnitService {

    @Override
    public List<ProductSkuSaleUnit> getByBarcode(String barcode) {
        return baseMapper.selectByBarcode(barcode);
    }

    @Override
    public List<ProductSkuSaleUnit> getBySkuNo(String skuNo) {
        return baseMapper.selectBySkuNo(skuNo);
    }

    @Override
    public List<ProductSkuSaleUnit> getBySkuId(Long skuId) {
        return baseMapper.selectBySkuId(skuId);
    }

    @Override
    public List<ProductSkuSaleUnit> getByTenantId(Long tenantId) {
        return baseMapper.selectByTenantId(tenantId);
    }

    @Override
    public List<ProductSkuSaleUnit> searchByGoodsName(String goodsName) {
        return baseMapper.selectByGoodsName(goodsName);
    }
}