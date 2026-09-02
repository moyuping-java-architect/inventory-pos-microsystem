package com.psi.purchase.mapper;

import com.psi.purchase.entity.PurchaseSyncLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchaseSyncLogMapper {

    PurchaseSyncLogEntity selectByType(@Param("type") String type);

    int updateLastTime(@Param("type") String type, @Param("lastTime") String lastTime);

    int insert(PurchaseSyncLogEntity entity);
}