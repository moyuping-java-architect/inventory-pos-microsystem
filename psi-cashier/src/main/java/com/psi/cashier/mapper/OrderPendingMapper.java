package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.OrderPendingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderPendingMapper extends BaseMapper<OrderPendingEntity> {

    @Select("SELECT * FROM order_pending WHERE operator_id = #{operatorId} AND tenant_id = #{tenantId}")
    List<OrderPendingEntity> selectByOperatorIdAndTenantId(@Param("operatorId") Integer operatorId, @Param("tenantId") String tenantId);

    @Select("SELECT * FROM order_pending WHERE operator_id = #{operatorId}")
    List<OrderPendingEntity> selectByOperatorId(@Param("operatorId") Integer operatorId);

    @Select("SELECT * FROM order_pending WHERE shop_code = #{shopCode}")
    List<OrderPendingEntity> selectByShopCode(@Param("shopCode") String shopCode);

    @Select("SELECT * FROM order_pending WHERE pos_id = #{posId}")
    List<OrderPendingEntity> selectByPosId(@Param("posId") String posId);

    @Select("SELECT * FROM order_pending WHERE tenant_id = #{tenantId}")
    List<OrderPendingEntity> selectByTenantId(@Param("tenantId") String tenantId);
}