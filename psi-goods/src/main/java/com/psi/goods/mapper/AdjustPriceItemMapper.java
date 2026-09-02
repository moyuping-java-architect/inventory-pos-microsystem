package com.psi.goods.mapper;

import com.psi.goods.entity.AdjustPriceItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品调价单明细表 Mapper
 */
@Mapper
public interface AdjustPriceItemMapper extends BaseMapper<AdjustPriceItemEntity> {

    /**
     * 根据调价单ID查询明细
     */
    @Select("SELECT * FROM goods_adjust_price_item WHERE adjust_id = #{adjustId} AND del_flag = 0 ORDER BY sort_order ASC, id ASC")
    List<AdjustPriceItemEntity> selectByAdjustId(@Param("adjustId") Long adjustId);
}
