package com.trademaster.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.stock.entity.StockOverflowItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockOverflowItemMapper extends BaseMapper<StockOverflowItem> {

    @Select("SELECT * FROM stock_overflow_item WHERE overflow_id = #{overflowId}")
    List<StockOverflowItem> selectByOverflowId(@Param("overflowId") Long overflowId);
}
