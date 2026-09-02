package com.psi.cashier.mapper;

import com.psi.cashier.entity.CashierShiftPayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 班次支付明细Mapper接口
 */
@Mapper
public interface CashierShiftPayMapper extends BaseMapper<CashierShiftPayEntity> {

    /**
     * 根据班次单号查询支付明细
     */
    @Select("SELECT * FROM cashier_shift_pay WHERE tenant_id = #{tenantId} AND shift_no = #{shiftNo}")
    List<CashierShiftPayEntity> selectByShiftNo(@Param("tenantId") String tenantId, @Param("shiftNo") String shiftNo);

    /**
     * 根据班次单号删除支付明细
     */
    @Delete("DELETE FROM cashier_shift_pay WHERE tenant_id = #{tenantId} AND shift_no = #{shiftNo}")
    void deleteByShiftNo(@Param("tenantId") String tenantId, @Param("shiftNo") String shiftNo);

    /**
     * 根据更新时间查询所有班次支付明细
     */
    @Select("SELECT * FROM cashier_shift_pay WHERE update_time > #{updateTime}")
    List<CashierShiftPayEntity> selectByUpdateTimeAfter(@Param("updateTime") String updateTime);

    @Select("<script>SELECT * FROM cashier_shift_pay WHERE shift_no IN <foreach collection='shiftNos' item='shiftNo' open='(' separator=',' close=')'>#{shiftNo}</foreach></script>")
    List<CashierShiftPayEntity> selectByShiftNos(@Param("shiftNos") List<String> shiftNos);
}