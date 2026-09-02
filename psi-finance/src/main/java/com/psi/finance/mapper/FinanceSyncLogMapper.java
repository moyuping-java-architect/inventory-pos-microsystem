package com.psi.finance.mapper;

import com.psi.finance.entity.FinanceSyncLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FinanceSyncLogMapper {

    FinanceSyncLogEntity selectByType(@Param("type") String type);

    int updateLastTime(@Param("type") String type, @Param("lastTime") String lastTime);

    int insert(FinanceSyncLogEntity entity);
}