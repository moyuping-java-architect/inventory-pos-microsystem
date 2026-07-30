package com.trademaster.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.stock.entity.StockLossItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockLossItemMapper extends BaseMapper<StockLossItem> {

    @Select("SELECT * FROM stock_loss_item WHERE loss_id = #{lossId}")
    List<StockLossItem> selectByLossId(@Param("lossId") Long lossId);
}
