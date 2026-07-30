package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.RefundPayEntity;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 退款支付Mapper接口
 * 提供退款支付数据访问操作
 * 
 * @author PSI
 * @version 1.0.0
 */
@Mapper
public interface RefundPayMapper extends BaseMapper<RefundPayEntity> {

    List<RefundPayEntity> selectByRefundNo(String refundNo);

    void deleteByRefundNo(String refundNo);

    @org.apache.ibatis.annotations.Select("SELECT * FROM refund_pay WHERE update_time > #{updateTime}")
    List<RefundPayEntity> selectByUpdateTimeAfter(@org.apache.ibatis.annotations.Param("updateTime") String updateTime);

    @org.apache.ibatis.annotations.Select("<script>SELECT * FROM refund_pay WHERE refund_no IN <foreach collection='refundNos' item='refundNo' open='(' separator=',' close=')'>#{refundNo}</foreach></script>")
    List<RefundPayEntity> selectByRefundNos(@org.apache.ibatis.annotations.Param("refundNos") List<String> refundNos);
}