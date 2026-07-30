package com.trademaster.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.sale.entity.SaleOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SaleOrderItemMapper extends BaseMapper<SaleOrderItem> {

    @Select("SELECT * FROM sale_order_item WHERE order_id = #{orderId}")
    List<SaleOrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
