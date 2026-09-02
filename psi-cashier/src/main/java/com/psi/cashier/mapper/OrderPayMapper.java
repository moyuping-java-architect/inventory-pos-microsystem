package com.psi.cashier.mapper;

import com.psi.cashier.entity.OrderPayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderPayMapper extends BaseMapper<OrderPayEntity> {

    @Select("SELECT * FROM order_pay WHERE order_no = #{orderNo}")
    List<OrderPayEntity> selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM order_pay WHERE update_time > #{updateTime}")
    List<OrderPayEntity> selectByUpdateTimeAfter(@Param("updateTime") String updateTime);

    @Select("<script>SELECT * FROM order_pay WHERE order_no IN <foreach collection='orderNos' item='orderNo' open='(' separator=',' close=')'>#{orderNo}</foreach></script>")
    List<OrderPayEntity> selectByOrderNos(@Param("orderNos") List<String> orderNos);
}