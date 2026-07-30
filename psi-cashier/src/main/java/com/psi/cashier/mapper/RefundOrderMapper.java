package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.RefundOrderEntity;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 退货订单Mapper接口
 * 提供退货订单数据访问操作
 * 
 * @author PSI
 * @version 1.0.0
 */
@Mapper
public interface RefundOrderMapper extends BaseMapper<RefundOrderEntity> {

    RefundOrderEntity selectByRefundNo(String refundNo);

    List<RefundOrderEntity> selectBySourceOrderNo(String sourceOrderNo);

    List<RefundOrderEntity> selectByOperatorId(Integer operatorId);

    @org.apache.ibatis.annotations.Select("SELECT * FROM refund_order WHERE update_time > #{updateTime}")
    List<RefundOrderEntity> selectByUpdateTimeAfter(@org.apache.ibatis.annotations.Param("updateTime") String updateTime);

    @org.apache.ibatis.annotations.Select("SELECT * FROM refund_order WHERE update_time > #{updateTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<RefundOrderEntity> selectByUpdateTimeAfterPage(@org.apache.ibatis.annotations.Param("updateTime") String updateTime,
            @org.apache.ibatis.annotations.Param("offset") int offset, @org.apache.ibatis.annotations.Param("limit") int limit);
}