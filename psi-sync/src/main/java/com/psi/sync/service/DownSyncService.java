package com.psi.sync.service;

import com.psi.sync.entity.DownSyncEntity;

import java.util.List;

/**
 * 下行同步服务接口
 * 进销存 → POS
 */
public interface DownSyncService {

    /**
     * 插入下行同步数据
     *
     * @param entity 下行同步实体
     * @return 是否插入成功
     */
    boolean insert(DownSyncEntity entity);

    /**
     * 批量插入下行同步数据
     *
     * @param entities 下行同步实体列表
     * @return 插入数量
     */
    int batchInsert(List<DownSyncEntity> entities);

    /**
     * 查询待下载的数据
     *
     * @param lastTime 上次拉取时间
     * @return 待下载数据列表
     */
    List<DownSyncEntity> getPendingDownload(String lastTime);

    /**
     * 更新下载状态
     *
     * @param batchUuid 批次UUID
     * @return 是否更新成功
     */
    boolean updateDownloadStatus(String batchUuid);

    /**
     * 批量更新下载状态
     *
     * @param batchUuids 批次UUID列表
     * @return 更新数量
     */
    int batchUpdateDownloadStatus(List<String> batchUuids);

    /**
     * 根据批次UUID查询
     *
     * @param batchUuid 批次UUID
     * @return 下行同步实体
     */
    DownSyncEntity getByBatchUuid(String batchUuid);
}