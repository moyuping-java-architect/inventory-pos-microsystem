package com.psi.goods.mapper;

import com.psi.goods.dto.CashierGoodsDTO;
import com.psi.goods.entity.GoodsSku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品SKU Mapper
 */
@Mapper
public interface GoodsSkuMapper extends BaseMapper<GoodsSku> {

    /**
     * 收银端按goods_unify_code分组查询商品
     * 同编码下取最高销售价展示，保证利润最大化
     */
    @Select("SELECT " +
            "    s.goods_unify_code, " +
            "    MAX(g.goods_name) as goods_name, " +
            "    MAX(s.sale_price) as max_sale_price, " +
            "    MIN(s.sale_price) as min_sale_price, " +
            "    AVG(s.cost_price) as avg_cost_price, " +
            "    SUM(s.stock_qty) as total_stock_qty, " +
            "    MAX(g.category_id) as category_id, " +
            "    MAX(g.brand_id) as brand_id, " +
            "    MAX(s.base_unit) as base_unit, " +
            "    MAX(s.sale_unit) as sale_unit, " +
            "    MAX(s.unit_conversion) as unit_conversion, " +
            "    MAX(g.image_url) as image_url, " +
            "    MAX(s.tax_rate) as tax_rate, " +
            "    MAX(s.is_tax_inclusive) as is_tax_inclusive, " +
            "    MAX(s.sale_price_usd) as sale_price_usd " +
            "FROM goods_sku s " +
            "LEFT JOIN goods g ON s.goods_id = g.id " +
            "WHERE s.status = 1 AND s.del_flag = 0 " +
            "  AND (s.goods_unify_code LIKE CONCAT('%', #{goodsUnifyCode}, '%') OR #{goodsUnifyCode} IS NULL) " +
            "  AND (g.goods_name LIKE CONCAT('%', #{goodsName}, '%') OR #{goodsName} IS NULL) " +
            "  AND (g.category_id = #{categoryId} OR #{categoryId} IS NULL) " +
            "  AND (g.brand_id = #{brandId} OR #{brandId} IS NULL) " +
            "  AND (${hasStockCondition}) " +
            "GROUP BY s.goods_unify_code " +
            "ORDER BY max_sale_price DESC " +
            "LIMIT #{offset}, #{limit})")
    List<CashierGoodsDTO> selectForCashier(
            @Param("goodsUnifyCode") String goodsUnifyCode,
            @Param("goodsName") String goodsName,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("hasStockCondition") String hasStockCondition,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 统计收银端商品数量
     */
    @Select("SELECT COUNT(DISTINCT s.goods_unify_code) " +
            "FROM goods_sku s " +
            "LEFT JOIN goods g ON s.goods_id = g.id " +
            "WHERE s.status = 1 AND s.del_flag = 0 " +
            "  AND (s.goods_unify_code LIKE CONCAT('%', #{goodsUnifyCode}, '%') OR #{goodsUnifyCode} IS NULL) " +
            "  AND (g.goods_name LIKE CONCAT('%', #{goodsName}, '%') OR #{goodsName} IS NULL) " +
            "  AND (g.category_id = #{categoryId} OR #{categoryId} IS NULL) " +
            "  AND (g.brand_id = #{brandId} OR #{brandId} IS NULL) " +
            "  AND (${hasStockCondition})")
    Long countForCashier(
            @Param("goodsUnifyCode") String goodsUnifyCode,
            @Param("goodsName") String goodsName,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("hasStockCondition") String hasStockCondition);

    /**
     * 根据goods_unify_code查询所有SKU
     */
    @Select("SELECT * FROM goods_sku WHERE goods_unify_code = #{goodsUnifyCode} AND status = 1 AND del_flag = 0 ORDER BY sale_price DESC")
    List<GoodsSku> selectByUnifyCode(@Param("goodsUnifyCode") String goodsUnifyCode);

    /**
     * 根据条码查询商品（返回最高价格）
     * 通过条码找到SKU，再关联goods主表获取商品信息
     */
    @Select("SELECT " +
            "    s.goods_unify_code, " +
            "    MAX(g.goods_name) as goods_name, " +
            "    MAX(s.sale_price) as max_sale_price, " +
            "    MIN(s.sale_price) as min_sale_price, " +
            "    AVG(s.cost_price) as avg_cost_price, " +
            "    SUM(s.stock_qty) as total_stock_qty, " +
            "    MAX(g.category_id) as category_id, " +
            "    MAX(g.brand_id) as brand_id, " +
            "    MAX(s.base_unit) as base_unit, " +
            "    MAX(s.sale_unit) as sale_unit, " +
            "    MAX(s.unit_conversion) as unit_conversion, " +
            "    MAX(g.image_url) as image_url, " +
            "    MAX(s.tax_rate) as tax_rate, " +
            "    MAX(s.is_tax_inclusive) as is_tax_inclusive, " +
            "    MAX(s.sale_price_usd) as sale_price_usd " +
            "FROM goods_sku s " +
            "LEFT JOIN goods g ON s.goods_id = g.id " +
            "WHERE s.status = 1 AND s.del_flag = 0 " +
            "  AND s.barcode = #{barcode} " +
            "GROUP BY s.goods_unify_code")
    List<CashierGoodsDTO> selectByBarcode(@Param("barcode") String barcode);

    /**
     * 根据条码获取对应的goods_unify_code
     */
    @Select("SELECT goods_unify_code FROM goods_sku WHERE barcode = #{barcode} AND status = 1 AND del_flag = 0 LIMIT 1")
    String selectUnifyCodeByBarcode(@Param("barcode") String barcode);

    /**
     * 根据更新时间分页查询（用于数据同步）
     */
    @Select("SELECT * FROM goods_sku WHERE update_time > #{lastTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<GoodsSku> selectByUpdateTimeAfterPage(@Param("lastTime") String lastTime, @Param("offset") int offset, @Param("limit") int limit);

}