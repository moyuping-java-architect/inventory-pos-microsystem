package com.psi.cashier.mapper;

import com.psi.cashier.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemEntity> {

    @Select("SELECT * FROM order_item WHERE order_no = #{orderNo}")
    List<OrderItemEntity> selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM order_item WHERE update_time > #{updateTime}")
    List<OrderItemEntity> selectByUpdateTimeAfter(@Param("updateTime") String updateTime);

    @Select("<script>SELECT * FROM order_item WHERE order_no IN <foreach collection='orderNos' item='orderNo' open='(' separator=',' close=')'>#{orderNo}</foreach></script>")
    List<OrderItemEntity> selectByOrderNos(@Param("orderNos") List<String> orderNos);
}