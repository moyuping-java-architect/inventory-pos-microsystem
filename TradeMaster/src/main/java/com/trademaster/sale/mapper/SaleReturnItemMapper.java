package com.trademaster.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.sale.entity.SaleReturnItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SaleReturnItemMapper extends BaseMapper<SaleReturnItem> {

    @Select("SELECT * FROM sale_return_item WHERE return_id = #{returnId}")
    List<SaleReturnItem> selectByReturnId(@Param("returnId") Long returnId);
}
