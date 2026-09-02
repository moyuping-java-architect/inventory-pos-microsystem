package com.psi.cashier.mapper;

import com.psi.cashier.entity.ProductSkuSaleUnit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * SKU销售单位Mapper
 */
@Mapper
public interface ProductSkuSaleUnitMapper extends BaseMapper<ProductSkuSaleUnit> {

    @Select("SELECT * FROM product_sku_sale_unit WHERE barcode = #{barcode} AND status = 1 AND del_flag = 0")
    List<ProductSkuSaleUnit> selectByBarcode(@Param("barcode") String barcode);

    @Select("SELECT * FROM product_sku_sale_unit WHERE sku_no = #{skuNo} AND status = 1 AND del_flag = 0")
    List<ProductSkuSaleUnit> selectBySkuNo(@Param("skuNo") String skuNo);

    @Select("SELECT * FROM product_sku_sale_unit WHERE sku_id = #{skuId} AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<ProductSkuSaleUnit> selectBySkuId(@Param("skuId") Long skuId);

    @Select("SELECT * FROM product_sku_sale_unit WHERE tenant_id = #{tenantId} AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<ProductSkuSaleUnit> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("SELECT * FROM product_sku_sale_unit WHERE goods_name LIKE CONCAT('%', #{goodsName}, '%') AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<ProductSkuSaleUnit> selectByGoodsName(@Param("goodsName") String goodsName);
}