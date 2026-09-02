package com.psi.goods.mapper;

import com.psi.goods.entity.GoodsSyncLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsSyncLogMapper {

    GoodsSyncLogEntity selectByType(@Param("type") String type);

    int updateLastTime(@Param("type") String type, @Param("lastTime") String lastTime);

    int insert(GoodsSyncLogEntity entity);
}