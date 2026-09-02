package com.psi.cashier.mapper;

import com.psi.cashier.entity.CashierShiftEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 班次结算Mapper接口
 */
@Mapper
public interface CashierShiftMapper extends BaseMapper<CashierShiftEntity> {

    /**
     * 根据班次单号查询
     */
    @Select("SELECT * FROM cashier_shift WHERE tenant_id = #{tenantId} AND shift_no = #{shiftNo}")
    CashierShiftEntity selectByShiftNo(@Param("tenantId") String tenantId, @Param("shiftNo") String shiftNo);

    /**
     * 根据收银员ID查询班次记录
     */
    @Select("SELECT * FROM cashier_shift WHERE tenant_id = #{tenantId} AND operator_id = #{operatorId} ORDER BY create_time DESC")
    List<CashierShiftEntity> selectByOperatorId(@Param("tenantId") String tenantId, @Param("operatorId") Integer operatorId);

    /**
     * 根据日期查询班次记录
     */
    @Select("SELECT * FROM cashier_shift WHERE tenant_id = #{tenantId} AND strftime('%Y-%m-%d', create_time) = #{dateStr} ORDER BY create_time DESC")
    List<CashierShiftEntity> selectByDate(@Param("tenantId") String tenantId, @Param("dateStr") String dateStr);

    /**
     * 查询收银员当天是否已有未完成的班次
     */
    @Select("SELECT COUNT(*) FROM cashier_shift WHERE tenant_id = #{tenantId} AND operator_id = #{operatorId} AND status = 0 AND strftime('%Y-%m-%d', create_time) = #{dateStr}")
    int countUnfinishedByOperator(@Param("tenantId") String tenantId, @Param("operatorId") Integer operatorId, @Param("dateStr") String dateStr);

    /**
     * 获取收银员最新的班次记录
     */
    @Select("SELECT * FROM cashier_shift WHERE tenant_id = #{tenantId} AND operator_id = #{operatorId} ORDER BY create_time DESC LIMIT 1")
    CashierShiftEntity selectLastByOperator(@Param("tenantId") String tenantId, @Param("operatorId") Integer operatorId);

    /**
     * 根据更新时间查询所有班次（不带tenantId条件）
     */
    @Select("SELECT * FROM cashier_shift WHERE update_time > #{updateTime}")
    List<CashierShiftEntity> selectAllByUpdateTimeAfter(@Param("updateTime") String updateTime);

    @Select("SELECT * FROM cashier_shift WHERE update_time > #{updateTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<CashierShiftEntity> selectByUpdateTimeAfterPage(@Param("updateTime") String updateTime,
            @Param("offset") int offset, @Param("limit") int limit);
}