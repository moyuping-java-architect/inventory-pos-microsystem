package com.psi.stock.mapper;

import com.psi.stock.entity.StockSyncLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockSyncLogMapper {

    StockSyncLogEntity selectByType(@Param("type") String type);

    int updateLastTime(@Param("type") String type, @Param("lastTime") String lastTime);

    int insert(StockSyncLogEntity entity);
}