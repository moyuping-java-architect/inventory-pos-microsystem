package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.RefundOrderItemEntity;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 退货明细Mapper接口
 * 提供退货明细数据访问操作
 * 
 * @author PSI
 * @version 1.0.0
 */
@Mapper
public interface RefundOrderItemMapper extends BaseMapper<RefundOrderItemEntity> {

    List<RefundOrderItemEntity> selectByRefundNo(String refundNo);

    void deleteByRefundNo(String refundNo);

    List<RefundOrderItemEntity> selectBySourceOrderNo(String sourceOrderNo);

    @org.apache.ibatis.annotations.Select("SELECT * FROM refund_order_item WHERE update_time > #{updateTime}")
    List<RefundOrderItemEntity> selectByUpdateTimeAfter(@org.apache.ibatis.annotations.Param("updateTime") String updateTime);

    @org.apache.ibatis.annotations.Select("<script>SELECT * FROM refund_order_item WHERE refund_no IN <foreach collection='refundNos' item='refundNo' open='(' separator=',' close=')'>#{refundNo}</foreach></script>")
    List<RefundOrderItemEntity> selectByRefundNos(@org.apache.ibatis.annotations.Param("refundNos") List<String> refundNos);
}