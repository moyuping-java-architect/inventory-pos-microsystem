package com.psi.sale.mapper;

import com.psi.sale.entity.CustomerSyncLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 客户同步日志 Mapper
 */
@Mapper
public interface CustomerSyncLogMapper {

    CustomerSyncLogEntity selectByType(@Param("type") String type);

    int updateLastTime(@Param("type") String type, @Param("lastTime") String lastTime);

    int insert(CustomerSyncLogEntity entity);
}