package com.psi.cashier.mapper;

import com.psi.cashier.entity.OrderMainEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMainMapper extends BaseMapper<OrderMainEntity> {

    @Select("SELECT * FROM order_main WHERE order_no = #{orderNo}")
    OrderMainEntity selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM order_main WHERE update_time > #{updateTime}")
    java.util.List<OrderMainEntity> selectByUpdateTimeAfter(@Param("updateTime") String updateTime);

    @Select("SELECT * FROM order_main WHERE update_time > #{updateTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    java.util.List<OrderMainEntity> selectByUpdateTimeAfterPage(@Param("updateTime") String updateTime, 
            @Param("offset") int offset, @Param("limit") int limit);
}