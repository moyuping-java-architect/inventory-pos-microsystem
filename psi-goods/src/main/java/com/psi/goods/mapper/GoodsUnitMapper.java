package com.psi.goods.mapper;

import com.psi.goods.entity.GoodsUnit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品单位 Mapper
 */
@Mapper
public interface GoodsUnitMapper extends BaseMapper<GoodsUnit> {

    /**
     * 根据单位类型查询单位列表
     */
    @Select("SELECT * FROM goods_unit WHERE unit_type = #{unitType} AND status = 1 AND del_flag = 0 ORDER BY sort_order")
    List<GoodsUnit> selectByUnitType(@Param("unitType") String unitType);

    /**
     * 查询所有启用的单位
     */
    @Select("SELECT * FROM goods_unit WHERE status = 1 AND del_flag = 0 ORDER BY unit_type, sort_order")
    List<GoodsUnit> selectAllEnabled();

    /**
     * 根据单位符号查询
     */
    @Select("SELECT * FROM goods_unit WHERE unit_symbol = #{unitSymbol} AND status = 1 AND del_flag = 0")
    GoodsUnit selectBySymbol(@Param("unitSymbol") String unitSymbol);

    /**
     * 根据更新时间分页查询（用于数据同步）
     */
    @Select("SELECT * FROM goods_unit WHERE update_time > #{lastTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<GoodsUnit> selectByUpdateTimeAfterPage(@Param("lastTime") String lastTime, @Param("offset") int offset, @Param("limit") int limit);
}