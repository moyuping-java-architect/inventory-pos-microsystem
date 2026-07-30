package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.OrderPendingItemEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderPendingItemMapper extends BaseMapper<OrderPendingItemEntity> {

    @Select("SELECT * FROM order_pending_item WHERE pending_no = #{pendingNo}")
    List<OrderPendingItemEntity> selectByPendingNo(@Param("pendingNo") String pendingNo);

    @Delete("DELETE FROM order_pending_item WHERE pending_no = #{pendingNo}")
    int deleteByPendingNo(@Param("pendingNo") String pendingNo);
}