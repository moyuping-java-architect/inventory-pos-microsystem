package com.psi.goods.mapper;

import com.psi.goods.entity.GoodsCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品分类 Mapper
 */
@Mapper
public interface GoodsCategoryMapper extends BaseMapper<GoodsCategory> {

    /**
     * 查询顶级分类（parent_id为null或0）
     */
    @Select("SELECT * FROM goods_category WHERE (parent_id IS NULL OR parent_id = 0) AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<GoodsCategory> selectTopLevel();

    /**
     * 根据父分类ID查询子分类
     */
    @Select("SELECT * FROM goods_category WHERE parent_id = #{parentId} AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<GoodsCategory> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有启用的分类（含层级）
     */
    @Select("SELECT * FROM goods_category WHERE status = 1 AND del_flag = 0 ORDER BY level, sort_order")
    List<GoodsCategory> selectAllEnabled();

    /**
     * 根据分类编码查询
     */
    @Select("SELECT * FROM goods_category WHERE category_code = #{categoryCode} AND status = 1 AND del_flag = 0")
    GoodsCategory selectByCode(@Param("categoryCode") String categoryCode);

    /**
     * 根据更新时间分页查询（用于数据同步）
     */
    @Select("SELECT * FROM goods_category WHERE update_time > #{lastTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<GoodsCategory> selectByUpdateTimeAfterPage(@Param("lastTime") String lastTime, @Param("offset") int offset, @Param("limit") int limit);
}