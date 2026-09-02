package com.psi.goods.mapper;

import com.psi.goods.entity.GoodsBrand;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品品牌 Mapper
 */
@Mapper
public interface GoodsBrandMapper extends BaseMapper<GoodsBrand> {

    /**
     * 查询所有启用的品牌
     */
    @Select("SELECT * FROM goods_brand WHERE status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<GoodsBrand> selectAllEnabled();

    /**
     * 根据品牌编码查询
     */
    @Select("SELECT * FROM goods_brand WHERE brand_code = #{brandCode} AND status = 1 AND del_flag = 0")
    GoodsBrand selectByCode(@Param("brandCode") String brandCode);

    /**
     * 根据品牌名称查询
     */
    @Select("SELECT * FROM goods_brand WHERE brand_name LIKE CONCAT('%', #{brandName}, '%') AND status = 1 AND del_flag = 0")
    List<GoodsBrand> selectByName(@Param("brandName") String brandName);

    /**
     * 根据更新时间分页查询（用于数据同步）
     */
    @Select("SELECT * FROM goods_brand WHERE update_time > #{lastTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<GoodsBrand> selectByUpdateTimeAfterPage(@Param("lastTime") String lastTime, @Param("offset") int offset, @Param("limit") int limit);
}