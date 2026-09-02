package com.psi.sync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.sync.entity.DownSyncEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 下行同步中间表Mapper接口
 */
@Mapper
public interface DownSyncMapper extends BaseMapper<DownSyncEntity> {

    /**
     * 查询待下载的数据
     *
     * @param lastTime 上次拉取时间
     * @return 待下载数据列表
     */
    List<DownSyncEntity> selectPendingDownload(@Param("lastTime") String lastTime);

    /**
     * 根据批次UUID列表批量更新下载状态
     *
     * @param batchUuids       批次UUID列表
     * @param syncStatus       同步状态
     * @param lastDownloadTime 最后下载时间
     * @return 更新行数
     */
    int batchUpdateDownloadStatusByBatchUuids(
            @Param("batchUuids") List<String> batchUuids,
            @Param("syncStatus") Integer syncStatus,
            @Param("lastDownloadTime") String lastDownloadTime);

    /**
     * 插入或更新下行同步数据（基于 data_uuid 和 data_version）
     * 使用 INSERT ... ON DUPLICATE KEY UPDATE 确保同一 data_uuid 只保留最新版本
     *
     * @param entity 下行同步实体
     * @return 受影响行数
     */
    int insertOrUpdate(@Param("entity") DownSyncEntity entity);
}