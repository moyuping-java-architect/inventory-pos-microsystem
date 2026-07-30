package com.psi.purchase.mapper;

import com.psi.purchase.entity.SyncLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SyncLogMapper {

    /**
     * 根据类型查询同步日志（up:上传, down:下载）
     */
    SyncLogEntity selectByType(@Param("type") String type);

    /**
     * 更新同步时间
     */
    int updateLastTime(@Param("type") String type, @Param("lastTime") String lastTime);

    /**
     * 插入同步日志
     */
    int insert(SyncLogEntity entity);
}
