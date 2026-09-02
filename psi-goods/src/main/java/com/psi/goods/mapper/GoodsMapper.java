package com.psi.goods.mapper;

import com.psi.goods.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品主表 Mapper
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 查询所有启用的商品
     */
    @Select("SELECT * FROM goods WHERE status = 1 AND del_flag = 0 ORDER BY create_time DESC")
    List<Goods> selectAllEnabled();

    /**
     * 根据商品编码查询
     */
    @Select("SELECT * FROM goods WHERE goods_code = #{goodsCode} AND status = 1 AND del_flag = 0")
    Goods selectByCode(@Param("goodsCode") String goodsCode);

    /**
     * 根据分类ID查询商品
     */
    @Select("SELECT * FROM goods WHERE category_id = #{categoryId} AND status = 1 AND del_flag = 0")
    List<Goods> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据品牌ID查询商品
     */
    @Select("SELECT * FROM goods WHERE brand_id = #{brandId} AND status = 1 AND del_flag = 0")
    List<Goods> selectByBrandId(@Param("brandId") Long brandId);

    /**
     * 根据商品名称模糊查询
     */
    @Select("SELECT * FROM goods WHERE goods_name LIKE CONCAT('%', #{goodsName}, '%') AND status = 1 AND del_flag = 0")
    List<Goods> selectByName(@Param("goodsName") String goodsName);

    /**
     * 根据更新时间分页查询（用于数据同步）
     */
    @Select("SELECT * FROM goods WHERE update_time > #{lastTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<Goods> selectByUpdateTimeAfterPage(@Param("lastTime") String lastTime, @Param("offset") int offset, @Param("limit") int limit);
}