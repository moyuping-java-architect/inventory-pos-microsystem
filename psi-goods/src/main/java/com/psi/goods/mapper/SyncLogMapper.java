package com.psi.goods.mapper;

import com.psi.goods.entity.SyncLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 同步日志 Mapper
 */
@Mapper
public interface SyncLogMapper {

    /**
     * 根据类型查询同步日志
     */
    SyncLogEntity selectByType(@Param("type") String type);

    /**
     * 更新最后同步时间
     */
    int updateLastTime(@Param("type") String type, @Param("lastTime") String lastTime);

    /**
     * 插入同步日志
     */
    int insert(SyncLogEntity entity);
}