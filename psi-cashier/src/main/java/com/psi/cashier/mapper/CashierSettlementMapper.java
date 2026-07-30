package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.CashierSettlementEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 日结Mapper接口
 * 提供日结数据访问操作
 * 
 * @author PSI
 * @version 1.0.0
 */
@Mapper
public interface CashierSettlementMapper extends BaseMapper<CashierSettlementEntity> {

    CashierSettlementEntity selectBySettleNo(String settleNo);

    List<CashierSettlementEntity> selectByOperatorId(Integer operatorId);

    List<CashierSettlementEntity> selectByDate(String dateStr);

    @org.apache.ibatis.annotations.Select("SELECT * FROM cashier_settlement WHERE update_time > #{updateTime}")
    List<CashierSettlementEntity> selectByUpdateTimeAfter(@org.apache.ibatis.annotations.Param("updateTime") String updateTime);

    @org.apache.ibatis.annotations.Select("SELECT * FROM cashier_settlement WHERE update_time > #{updateTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<CashierSettlementEntity> selectByUpdateTimeAfterPage(@org.apache.ibatis.annotations.Param("updateTime") String updateTime,
            @org.apache.ibatis.annotations.Param("offset") int offset, @org.apache.ibatis.annotations.Param("limit") int limit);
}