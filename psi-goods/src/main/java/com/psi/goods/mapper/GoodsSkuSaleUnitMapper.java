package com.psi.goods.mapper;

import com.psi.goods.dto.CashierSaleUnitDTO;
import com.psi.goods.entity.GoodsSkuSaleUnit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * SKU销售单位Mapper
 */
@Mapper
public interface GoodsSkuSaleUnitMapper extends BaseMapper<GoodsSkuSaleUnit> {

    /**
     * 根据SKU ID查询所有销售单位
     */
    @Select("SELECT * FROM goods_sku_sale_unit WHERE sku_id = #{skuId} AND del_flag = 0 ORDER BY sort_order")
    List<GoodsSkuSaleUnit> selectBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据SKU ID查询启用的销售单位
     */
    @Select("SELECT * FROM goods_sku_sale_unit WHERE sku_id = #{skuId} AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<GoodsSkuSaleUnit> selectEnabledBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据SKU ID查询默认销售单位
     */
    @Select("SELECT * FROM goods_sku_sale_unit WHERE sku_id = #{skuId} AND is_default = 1 AND status = 1 AND del_flag = 0")
    GoodsSkuSaleUnit selectDefaultBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据SKU ID删除所有销售单位（逻辑删除）
     */
    @Update("UPDATE goods_sku_sale_unit SET del_flag = 1, update_time = NOW() WHERE sku_id = #{skuId}")
    int deleteBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据销售单位ID查询所有关联的SKU销售单位
     */
    @Select("SELECT * FROM goods_sku_sale_unit WHERE sale_unit_id = #{unitId} AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<GoodsSkuSaleUnit> selectByUnitId(@Param("unitId") Long unitId);

    /**
     * 根据商品统一编码查询所有销售单位
     */
    @Select("SELECT * FROM goods_sku_sale_unit WHERE goods_unify_code = #{goodsUnifyCode} AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<GoodsSkuSaleUnit> selectByUnifyCode(@Param("goodsUnifyCode") String goodsUnifyCode);

    /**
     * 根据商品统一编码分组查询各销售单位的最高价格
     * 用于收银端展示，保证利润最大化
     */
    @Select("SELECT " +
            "    goods_unify_code, " +
            "    sale_unit_id, " +
            "    sale_unit_name, " +
            "    sale_unit_symbol, " +
            "    MAX(sale_price) as sale_price, " +
            "    SUM(stock_qty) as stock_qty, " +
            "    MAX(conversion_rate) as conversion_rate, " +
            "    MAX(package_spec) as package_spec " +
            "FROM goods_sku_sale_unit " +
            "WHERE goods_unify_code = #{goodsUnifyCode} AND status = 1 AND del_flag = 0 " +
            "GROUP BY goods_unify_code, sale_unit_id, sale_unit_name, sale_unit_symbol " +
            "ORDER BY sort_order")
    List<GoodsSkuSaleUnit> selectMaxPriceByUnifyCode(@Param("goodsUnifyCode") String goodsUnifyCode);

    /**
     * 收银端查询商品销售单位（只查询一张表，性能最优）
     * 按goods_unify_code分组，同编码下各销售单位取最高价格
     */
    @Select("SELECT " +
            "    s.goods_unify_code, " +
            "    MAX(gs.goods_id) as goods_id, " +
            "    MAX(g.goods_code) as goods_code, " +
            "    MAX(s.sku_id) as sku_id, " +
            "    MAX(s.sku_code) as sku_code, " +
            "    MAX(s.barcode) as barcode, " +
            "    MAX(s.goods_name) as goods_name, " +
            "    s.sale_unit_id, " +
            "    s.sale_unit_name, " +
            "    s.sale_unit_symbol, " +
            "    MAX(s.sale_price) as sale_price, " +
            "    AVG(s.cost_price) as cost_price, " +
            "    SUM(s.stock_qty) as stock_qty, " +
            "    MAX(s.conversion_rate) as conversion_rate, " +
            "    MAX(s.package_spec) as package_spec, " +
            "    MAX(s.category_id) as category_id, " +
            "    MAX(s.brand_id) as brand_id, " +
            "    MAX(s.image_url) as image_url, " +
            "    MAX(s.is_default) as is_default " +
            "FROM goods_sku_sale_unit s " +
            "LEFT JOIN goods_sku gs ON s.sku_id = gs.id AND gs.del_flag = 0 " +
            "LEFT JOIN goods g ON gs.goods_id = g.id AND g.del_flag = 0 " +
            "WHERE s.status = 1 AND s.del_flag = 0 " +
            "  AND (s.goods_unify_code LIKE CONCAT('%', #{goodsUnifyCode}, '%') OR #{goodsUnifyCode} IS NULL) " +
            "  AND (s.goods_name LIKE CONCAT('%', #{goodsName}, '%') OR #{goodsName} IS NULL) " +
            "  AND (s.category_id = #{categoryId} OR #{categoryId} IS NULL) " +
            "  AND (s.brand_id = #{brandId} OR #{brandId} IS NULL) " +
            "GROUP BY s.goods_unify_code, s.sale_unit_id, s.sale_unit_name, s.sale_unit_symbol " +
            "ORDER BY MAX(s.sale_price) DESC " +
            "LIMIT #{offset}, #{limit}")
    List<CashierSaleUnitDTO> selectForCashier(
            @Param("goodsUnifyCode") String goodsUnifyCode,
            @Param("goodsName") String goodsName,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 统计收银端销售单位数量（只查询一张表）
     */
    @Select("SELECT COUNT(DISTINCT CONCAT(s.goods_unify_code, '-', s.sale_unit_id)) " +
            "FROM goods_sku_sale_unit s " +
            "WHERE s.status = 1 AND s.del_flag = 0 " +
            "  AND (s.goods_unify_code LIKE CONCAT('%', #{goodsUnifyCode}, '%') OR #{goodsUnifyCode} IS NULL) " +
            "  AND (s.goods_name LIKE CONCAT('%', #{goodsName}, '%') OR #{goodsName} IS NULL) " +
            "  AND (s.category_id = #{categoryId} OR #{categoryId} IS NULL) " +
            "  AND (s.brand_id = #{brandId} OR #{brandId} IS NULL)")
    Long countForCashier(
            @Param("goodsUnifyCode") String goodsUnifyCode,
            @Param("goodsName") String goodsName,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId);

    /**
     * 根据条码查询销售单位（返回最高价格，只查询一张表）
     * 用于收银端扫码查询，保证利润最大化
     */
    @Select("SELECT " +
            "    s.goods_unify_code, " +
            "    MAX(s.sku_id) as sku_id, " +
            "    MAX(s.sku_code) as sku_code, " +
            "    MAX(s.barcode) as barcode, " +
            "    MAX(s.goods_name) as goods_name, " +
            "    s.sale_unit_id, " +
            "    s.sale_unit_name, " +
            "    s.sale_unit_symbol, " +
            "    MAX(s.sale_price) as sale_price, " +
            "    AVG(s.cost_price) as cost_price, " +
            "    SUM(s.stock_qty) as stock_qty, " +
            "    MAX(s.conversion_rate) as conversion_rate, " +
            "    MAX(s.package_spec) as package_spec, " +
            "    MAX(s.category_id) as category_id, " +
            "    MAX(s.brand_id) as brand_id, " +
            "    MAX(s.image_url) as image_url, " +
            "    MAX(s.is_default) as is_default " +
            "FROM goods_sku_sale_unit s " +
            "WHERE s.status = 1 AND s.del_flag = 0 " +
            "  AND s.barcode = #{barcode} " +
            "GROUP BY s.goods_unify_code, s.sale_unit_id, s.sale_unit_name, s.sale_unit_symbol " +
            "ORDER BY MAX(s.sale_price) DESC")
    List<CashierSaleUnitDTO> selectByBarcode(@Param("barcode") String barcode);

    /**
     * 根据更新时间分页查询（用于数据同步）
     */
    @Select("SELECT * FROM goods_sku_sale_unit WHERE update_time > #{lastTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<GoodsSkuSaleUnit> selectByUpdateTimeAfterPage(@Param("lastTime") String lastTime, @Param("offset") int offset, @Param("limit") int limit);
}