package com.trademaster.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.sale.entity.SaleOutItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SaleOutItemMapper extends BaseMapper<SaleOutItem> {

    @Select("SELECT * FROM sale_out_item WHERE out_id = #{outId}")
    List<SaleOutItem> selectByOutId(@Param("outId") Long outId);
}
