package com.trademaster.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.stock.entity.StockTransferItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockTransferItemMapper extends BaseMapper<StockTransferItem> {

    @Select("SELECT * FROM stock_transfer_item WHERE transfer_id = #{transferId}")
    List<StockTransferItem> selectByTransferId(@Param("transferId") Long transferId);
}
