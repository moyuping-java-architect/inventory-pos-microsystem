package com.trademaster.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.stock.entity.StockCheckItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockCheckItemMapper extends BaseMapper<StockCheckItem> {

    @Select("SELECT * FROM stock_check_item WHERE check_id = #{checkId}")
    List<StockCheckItem> selectByCheckId(@Param("checkId") Long checkId);
}
